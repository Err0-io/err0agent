/*
Copyright 2023 ERR0 LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.err0.client.languages;

import com.google.gson.JsonArray;
import io.err0.client.Main;
import io.err0.client.core.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.err0.client.Main.reWhitespace;

public class LuaSourceCodeParse extends SourceCodeParse {

    public LuaSourceCodeParse(final CodePolicy policy)
    {
        super(Language.LUA, policy, policy.adv_lua);
        switch (policy.mode) {
            case DEFAULTS:
                reLogger = Pattern.compile("(^|\\s+|;)(m?_?)*log(ger)?\\.(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info|fault|notice)\\s*\\(\\s*?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                break;

            case EASY_CONFIGURATION:
            case ADVANCED_CONFIGURATION:
                reLogger = Pattern.compile("(^|\\s+|;)" + policy.easyModeObjectPattern() + "\\." + policy.easyModeMethodPattern() + "\\s*\\(\\s*?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                break;
        }

        final String pattern = policy.mode == CodePolicy.CodePolicyMode.DEFAULTS ? "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info|fault|notice)" : policy.easyModeMethodPattern();
        reLoggerLevel = Pattern.compile("\\.(" + pattern + ")\\s*\\(\\s*?$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); // group #1 is the level
    }

    private static Pattern reMethod = Pattern.compile("^(\\s*)((.*\\s+=\\s+)?(local\\s+)?function|(else)?if|else|for|while|repeat|until)\\s+.*$");
    private static Pattern reControl = Pattern.compile("(^|\\s+)((else)?if|else|for|while|repeat|until)(|\\s|$)", Pattern.MULTILINE);
    private Pattern reLogger = null;
    private Pattern reLoggerLevel = null;
    private static Pattern reException = Pattern.compile("(^|\\s+|;)error\\s*\\($");

    public static LuaSourceCodeParse lex(final CodePolicy policy, final String sourceCode) {
        int n = 0;
        LuaSourceCodeParse parse = new LuaSourceCodeParse(policy);
        Token currentToken = new Token(n++, null);
        currentToken.type = TokenType.SOURCE_CODE;
        int lineNumber = 1;
        int indentNumber = 0;
        boolean countIndent = true;
        boolean countNextIndent = false;
        currentToken.startLineNumber = lineNumber;
        final char chars[] = sourceCode.toCharArray();
        for (int i = 0, l = chars.length; i < l; ++i) {
            if (countNextIndent) {
                countIndent = true;
                countNextIndent = false;
            }
            final char ch = chars[i];
            boolean precedingCharactersAreIndent = countIndent;
            if (ch == '\n') {
                ++lineNumber;
                indentNumber = 0;
                countNextIndent = true;
            } else if (ch == '\t' && countIndent) {
                indentNumber = ((indentNumber / 8) + 1) * 8; // tab is 8 spaces
            } else if (ch == ' ' && countIndent) {
                ++indentNumber;
            } else {
                countIndent = false;
            }

            if (countIndent) {
                currentToken.depth = indentNumber;
            }

            int longBracketLevel = 0;
            switch (currentToken.type) {
                case SOURCE_CODE:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\'') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.APOS_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\"') {
                        // otherwise, it is a quot literal.
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.QUOT_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if ((longBracketLevel = isOpeningLongBracket(chars, i)) >= 0) {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.LONGBRACKET_LITERAL;
                        currentToken.longBracketLevel = longBracketLevel;
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[++i]);
                        for (int j = 0; j < longBracketLevel; ++j)
                            currentToken.sourceCode.append(chars[++i]);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (precedingCharactersAreIndent && ch == '-' && (i+1 < l && chars[i+1] == '-')) {
                        if (l > i + 3) {
                            if (chars[i+2] == '[' && chars[i+3] == '[') {
                                parse.tokenList.add(currentToken.finish(lineNumber));
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenType.COMMENT_BLOCK;
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(chars[++i]);
                                currentToken.sourceCode.append(chars[++i]);
                                currentToken.sourceCode.append(chars[++i]);
                                currentToken.depth = indentNumber;
                                currentToken.startLineNumber = lineNumber;
                                break;
                            }
                        }
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.COMMENT_LINE;
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[++i]);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case COMMENT_LINE:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case COMMENT_BLOCK:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.COMMENT_BLOCK;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (reWhitespace.matcher(currentToken.sourceCode).matches() && l > i + 3 && ch == '-' && chars[i+1] == '-' && chars[i+2] == ']' && chars[i+3] == ']') {
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[++i]);
                        currentToken.sourceCode.append(chars[++i]);
                        currentToken.sourceCode.append(chars[++i]);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case APOS_LITERAL:
                    if (ch == '\'') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\\') {
                        currentToken.sourceCode.append(ch);
                        final char ch2 = chars[++i];
                        currentToken.sourceCode.append(ch2);
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case QUOT_LITERAL:
                    if (ch == '\"') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\\') {
                        currentToken.sourceCode.append(ch);
                        final char ch2 = chars[++i];
                        currentToken.sourceCode.append(ch2);
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case LONGBRACKET_LITERAL:
                    if (isClosingLongBracket(chars, i, currentToken.longBracketLevel)) {
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[++i]);
                        for (int j = 0; j < longBracketLevel; ++j)
                            currentToken.sourceCode.append(chars[++i]);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
            }
        }
        parse.tokenList.add(currentToken.finish(lineNumber));
        return parse;
    }

    private static int isOpeningLongBracket(char chars[], int i) {
        int longBracketLevel = 0;
        if (chars[i++] == '[') {
            for(;;++i) {
                if (chars[i] == '[') {
                    return longBracketLevel;
                } else if (chars[i] != '=') {
                    return -1;
                }
                ++longBracketLevel;
            }
        } else {
            return -1;
        }
    }

    private static boolean isClosingLongBracket(char chars[], int i, int longBracketLevel) {
        if (chars[i++] != ']')
            return false;
        for (;longBracketLevel>0;--longBracketLevel) {
            if (chars[i++] != '=')
                return false;
        }
        if (chars[i++] != ']')
            return false;
        return true;
    }

    @Override
    public boolean couldContainErrorNumber(Token token) {
        return token.type == TokenType.APOS_LITERAL || token.type == TokenType.QUOT_LITERAL || token.type == TokenType.LONGBRACKET_LITERAL;
    }

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case APOS_LITERAL:
                case QUOT_LITERAL:
                case LONGBRACKET_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_lua().matcher(token.source);
                    boolean found = matcherErrorNumber.find();
                    if (found) {
                        token.classification = Token.Classification.ERROR_NUMBER;
                        final String quot = matcherErrorNumber.group(1);
                        long errorOrdinal = Long.parseLong(matcherErrorNumber.group(3));
                        if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                            if (globalState.store(errorOrdinal, stateItem, token)) {
                                token.keepErrorCode = true;
                                token.errorOrdinal = errorOrdinal;
                                token.sourceNoErrorCode = quot + token.source.substring(matcherErrorNumber.end());
                            } else {
                                token.sourceNoErrorCode = token.source = quot + token.source.substring(matcherErrorNumber.end());
                            }
                        } else {
                            token.sourceNoErrorCode = token.source = quot + token.source.substring(matcherErrorNumber.end());
                        }
                    } else if (policy.getCodePolicy().getEnablePlaceholder() && (token.type != TokenType.LONGBRACKET_LITERAL)) {
                        Matcher matcherPlaceholder = policy.getReErrorNumber_lua_placeholder().matcher(token.source);
                        if (matcherPlaceholder.matches()) {
                            token.classification = Token.Classification.PLACEHOLDER;
                            String number = matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_number_group);
                            if (null != number && ! "".equals(number)) {
                                long errorOrdinal = Long.parseLong(number);
                                if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                                    if (globalState.store(errorOrdinal, stateItem, token)) {
                                        token.keepErrorCode = true;
                                        token.errorOrdinal = errorOrdinal;
                                        token.sourceNoErrorCode = matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group);
                                    } else {
                                        token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group);
                                    }
                                } else {
                                    token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group);
                                }
                            } else {
                                token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_lua_placeholder_open_close_group);
                            }
                        } else {
                            token.classification = Token.Classification.POTENTIAL_ERROR_NUMBER;
                            token.sourceNoErrorCode = token.source;
                        }
                    } else {
                        token.classification = Token.Classification.POTENTIAL_ERROR_NUMBER;
                        token.sourceNoErrorCode = token.source;
                    }
                }
                break;
                case SOURCE_CODE:
                {
                    token.classification = Token.Classification.NOT_FULLY_CLASSIFIED;
                    Token next = token.next();
                    if (next != null && (next.type == TokenType.QUOT_LITERAL || next.type == TokenType.APOS_LITERAL || next.type == TokenType.LONGBRACKET_LITERAL)) {
                        // rule 0 - this code must be followed by a string literal
                        if (null != languageCodePolicy && languageCodePolicy.rules.size() > 0) {
                            // classify according to rules.
                            final String stringLiteral = next.getStringLiteral();
                            // now, also notice that the error code potential was detected first, so next has been evaluated already...
                            String lineOfCode = null;
                            if (Main.USE_NEAREST_CODE_FOR_LINE_OF_CODE) {
                                lineOfCode = token.source;
                            } else {
                                JsonArray lineArray = getNLinesOfContext(token.startLineNumber, 0, Main.CHAR_RADIUS);
                                if (null != lineArray && lineArray.size() > 0) {
                                    lineOfCode = GsonHelper.asString(lineArray.get(0).getAsJsonObject(), "c", null);
                                }
                            }
                            final LanguageCodePolicy.ClassificationResult classificationResult = languageCodePolicy.classify(lineOfCode, stringLiteral);
                            token.classification = classificationResult.classification;
                            token.loggerLevel = classificationResult.loggerLevel;
                        }
                    } else {
                        token.classification = Token.Classification.NO_MATCH;
                    }

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (null == languageCodePolicy || !languageCodePolicy.disable_builtin_log_detection) && (!codePolicy.getDisableLogs())) {
                        Matcher matcherLogger = reLogger.matcher(token.source);
                        if (matcherLogger.find()) {
                            token.classification = Token.Classification.LOG_OUTPUT;
                            // extract canonical log level meta data
                            Matcher matcherLoggerLevel = reLoggerLevel.matcher(token.source);
                            if (matcherLoggerLevel.find()) {
                                token.loggerLevel = matcherLoggerLevel.group(1);
                            }
                        }
                    }

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.NOT_LOG_OUTPUT || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (!codePolicy.getDisableExceptions())) {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                        }
                    }

                    if (token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION && (!codePolicy.getDisableLogs())) token.classification = Token.Classification.LOG_OUTPUT;

                    // message categorisation, dynamic
                    if (token.classification == Token.Classification.EXCEPTION_THROW || token.classification == Token.Classification.LOG_OUTPUT) {
                        if (null != token.next()) {
                            // note token current is a type of string literal.
                            boolean staticLiteral = true;
                            Token current = token.next();
                            StringBuilder cleaned = new StringBuilder();
                            StringBuilder output = new StringBuilder();
                            int bracketDepth = 1; // we are already one bracket into the expression.
                            boolean exit = false;
                            do {
                                final String sourceCode = null != current.sourceNoErrorCode ? current.sourceNoErrorCode : current.source;
                                switch (current.type) {
                                    case SOURCE_CODE:
                                        boolean dynamic = false;
                                        final char chars[] = sourceCode.toCharArray();
                                        for (int i = 0, l = chars.length; i < l; ++i) {
                                            final char ch = chars[i];
                                            if (ch == ')') {
                                                if (--bracketDepth < 1) {
                                                    break;
                                                }
                                            } else if (ch == '(') {
                                                ++bracketDepth;
                                            } else if (token.classification == Token.Classification.EXCEPTION_THROW && ch == ',') {
                                                // second parameter in error() is an error level, e.g. 1, 2, 3.
                                                exit = true;
                                                break;
                                            }
                                            if (!Character.isWhitespace(ch)) cleaned.append(ch);
                                            output.append(ch);
                                            if (!(Character.isWhitespace(ch) || ch == '.')) { // string concatenation
                                                dynamic = true;
                                            }
                                        }
                                        if (dynamic) {
                                            staticLiteral = false;
                                        }
                                        break;
                                    case COMMENT_BLOCK:
                                    case CONTENT:
                                    case COMMENT_LINE:
                                        break;
                                    default:
                                        cleaned.append(sourceCode);
                                        output.append(sourceCode);
                                        break;
                                }
                                if (bracketDepth < 1) {
                                    break;
                                }
                                if (exit) {
                                    break;
                                }
                            }
                            while (null != (current = current.next()));

                            token.staticLiteral = staticLiteral;
                            token.cleanedMessageExpression = cleaned.toString();
                            token.messageExpression = output.toString();
                        }
                    }
                }
                break;
                default:
                    token.classification = Token.Classification.NO_MATCH;
            }
        }
    }

    @Override
    public void classifyForCallStack(Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET || token.classification == Token.Classification.NOT_FULLY_CLASSIFIED) {
            if (token.type == TokenType.SOURCE_CODE) {
                Matcher matcherMethod = reMethod.matcher(token.source);
                if (matcherMethod.find()) {
                    final String code = matcherMethod.group();
                    //if (reMethodIgnore.matcher(code).find())
                    //    continue;
                    token.classification = Token.Classification.METHOD_SIGNATURE;
                    token.extractedCode = code;
                    if (reControl.matcher(token.extractedCode).find()) {
                        token.classification = Token.Classification.CONTROL_SIGNATURE;
                    }
                } else {
                    token.classification = Token.Classification.NO_MATCH;
                }
            } else {
                token.classification = Token.Classification.NO_MATCH;
            }
        }
    }
}
