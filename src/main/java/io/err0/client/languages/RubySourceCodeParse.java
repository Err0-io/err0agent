/*
Copyright 2022 BlueTrailSoftware, Holding Inc.

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
import io.err0.client.languages.ext.RubyExtendedInformation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RubySourceCodeParse extends SourceCodeParse {

    public RubySourceCodeParse(final CodePolicy policy)
    {
        super(Language.RUBY, policy, policy.adv_ruby);
        switch (policy.mode) {
            case DEFAULTS:
                reLogger = Pattern.compile("(^|\\s+)(m?_?)*log(ger)?\\.(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)\\s+$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                break;

            case EASY_CONFIGURATION:
            case ADVANCED_CONFIGURATION:
                reLogger = Pattern.compile("(^|\\s+)" + policy.easyModeObjectPattern() + "\\." + policy.easyModeMethodPattern() + "\\s+$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                break;
        }

        final String pattern = policy.mode == CodePolicy.CodePolicyMode.DEFAULTS ? "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)" : policy.easyModeMethodPattern();
        reLoggerLevel = Pattern.compile("\\.(" + pattern + ")\\s+$", Pattern.CASE_INSENSITIVE); // group #1 is the level
    }

    private static Pattern reMethod = Pattern.compile("^(\\s*)(def|class|if|unless|for|while|except)\\s+.*$");
    private Pattern reLogger = null;
    private Pattern reLoggerLevel = null;
    private static Pattern reException = Pattern.compile("(^|\\s+)raise\\s(\\S+),\\s+$");
    private static int reException_group_class = 2;
    private static final char[] percentLiteralCharacters = { 'q', 'Q', 'w', 'W', 'i', 'I', 's' };

    public static RubySourceCodeParse lex(final CodePolicy policy, final String sourceCode) {
        int n = 0;
        RubySourceCodeParse parse = new RubySourceCodeParse(policy);
        Token currentToken = new Token(n++, null);
        currentToken.type = TokenClassification.SOURCE_CODE;
        int lineNumber = 1;
        int indentNumber = 0;
        boolean countIndent = true;
        currentToken.startLineNumber = lineNumber;
        final char chars[] = sourceCode.toCharArray();
        for (int i = 0, l = chars.length; i < l; ++i) {
            final char ch = chars[i];
            if (ch == '\n') {
                ++lineNumber;
                indentNumber = 0;
            } else if (ch == '\t' && countIndent) {
                indentNumber = ((indentNumber/8)+1)*8; // tab is 8 spaces
            } else if (ch == ' ' && countIndent) {
                ++indentNumber;
            } else {
                countIndent = false;
            }

            if (countIndent) {
                currentToken.depth = indentNumber;
            }

            switch (currentToken.type) {
                case SOURCE_CODE:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        countIndent = true;
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\'') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.APOS_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\"') {
                        // otherwise, it is a quot literal.
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.QUOT_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '#') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.COMMENT_LINE;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '%') {
                        char opening = 0;
                        char closing = 0;
                        if (i + 1 < chars.length) {
                            char nextChar = chars[i + 1];
                            char found = 0;
                            for (char c : percentLiteralCharacters) {
                                if (c == nextChar) {
                                    found = c;
                                    break;
                                }
                            }
                            if (found == 0 || found == 'Q' || found == 'q') {
                                if (i + (found == 0 ? 1 : 2) < chars.length) {
                                    nextChar = chars[i + (found == 0 ? 1 : 2)];
                                    switch (nextChar) {
                                        case '{':
                                            opening = '{';
                                            closing = '}';
                                            break;
                                        case '[':
                                            opening = '[';
                                            closing = ']';
                                            break;
                                        case '(':
                                            opening = '(';
                                            closing = ')';
                                            break;
                                        case '<':
                                            opening = '<';
                                            closing = '>';
                                            break;
                                        default:
                                            if (!Character.isWhitespace(nextChar) && !Character.isLetter(nextChar) && !Character.isDigit(nextChar) && nextChar != '\\') {
                                                opening = nextChar;
                                                closing = nextChar;
                                            }
                                    }
                                }
                                if (opening != 0 && closing != 0) {
                                    parse.tokenList.add(currentToken.finish(lineNumber));
                                    currentToken = new Token(n++, currentToken);
                                    currentToken.type = found == 'q' ? TokenClassification.APOS_LITERAL : TokenClassification.QUOT_LITERAL;
                                    currentToken.sourceCode.append(ch);
                                    currentToken.sourceCode.append(chars[++i]);
                                    if (found != 0) {
                                        currentToken.sourceCode.append(chars[++i]);
                                    }
                                    currentToken.depth = indentNumber;
                                    currentToken.startLineNumber = lineNumber;
                                    RubyExtendedInformation extendedInformation = new RubyExtendedInformation(currentToken);
                                    extendedInformation.percentLiteral = true;
                                    extendedInformation.percentLiteralType = found;
                                    extendedInformation.opening = opening;
                                    extendedInformation.closing = closing;
                                    currentToken.extendedInformation = extendedInformation;
                                }
                            }
                        }
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case COMMENT_LINE:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        countIndent = true;
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case APOS_LITERAL:
                    if (null != currentToken.extendedInformation) {
                        RubyExtendedInformation extendedInformation = (RubyExtendedInformation) currentToken.extendedInformation;
                        if (extendedInformation.percentLiteral) {
                            if (ch == '\\' && i + 1 < chars.length) {
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(chars[++i]);
                            } else if (ch == extendedInformation.closing) {
                                currentToken.sourceCode.append(ch);
                                parse.tokenList.add(currentToken.finish(lineNumber));
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.SOURCE_CODE;
                                currentToken.depth = indentNumber;
                                currentToken.startLineNumber = lineNumber;
                            } else {
                                currentToken.sourceCode.append(ch);
                            }
                        } else {
                            throw new RuntimeException("[AGENT-000096] Illegal state.");
                        }
                    } else {
                        if (ch == '\'') {
                            currentToken.sourceCode.append(ch);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.depth = indentNumber;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == '\\') {
                            currentToken.sourceCode.append(ch);
                            final char ch2 = chars[++i];
                            currentToken.sourceCode.append(ch2);
                        } else {
                            currentToken.sourceCode.append(ch);
                        }
                    }
                    break;
                case QUOT_LITERAL:
                    if (null != currentToken.extendedInformation) {
                        RubyExtendedInformation extendedInformation = (RubyExtendedInformation) currentToken.extendedInformation;
                        if (extendedInformation.percentLiteral) {
                            if (ch == '\\' && i + 1 < chars.length) {
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(chars[++i]);
                            } else if (ch == '#' && i + 1 < chars.length && chars[i + 1] == '{') {
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(chars[++i]);
                                extendedInformation.curlyBracketIndent++;
                            } else if (extendedInformation.curlyBracketIndent > 0 && ch == '}') {
                                currentToken.sourceCode.append(ch);
                                extendedInformation.curlyBracketIndent--;
                            } else if (ch == extendedInformation.closing) {
                                currentToken.sourceCode.append(ch);
                                parse.tokenList.add(currentToken.finish(lineNumber));
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.SOURCE_CODE;
                                currentToken.depth = indentNumber;
                                currentToken.startLineNumber = lineNumber;
                            } else {
                                currentToken.sourceCode.append(ch);
                            }
                        } else {
                            throw new RuntimeException("[AGENT-000097] Illegal state.");
                        }
                    } else {
                        if (ch == '\"') {
                            currentToken.sourceCode.append(ch);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.depth = indentNumber;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == '\\') {
                            currentToken.sourceCode.append(ch);
                            final char ch2 = chars[++i];
                            currentToken.sourceCode.append(ch2);
                        } else {
                            currentToken.sourceCode.append(ch);
                        }
                    }
                    break;
            }
        }
        parse.tokenList.add(currentToken.finish(lineNumber));
        return parse;
    }

    @Override
    public boolean couldContainErrorNumber(Token token) {
        return token.type == TokenClassification.APOS_LITERAL || token.type == TokenClassification.BACKTICK_LITERAL || token.type == TokenClassification.QUOT_LITERAL;
    }

    private static Pattern reContinuation = Pattern.compile("\\\\$");
    private static Pattern reIfUnless = Pattern.compile(" (if|unless) ");

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case APOS_LITERAL:
                case QUOT_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_rb().matcher(token.source);
                    boolean found = matcherErrorNumber.find();

                    if (found) {
                        token.classification = Token.Classification.ERROR_NUMBER;
                        final String quot = matcherErrorNumber.group(1);
                        long errorOrdinal = Long.parseLong(matcherErrorNumber.group(2));
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
                    while (null != next && next.type == TokenClassification.SOURCE_CODE && Main.reWhitespace.matcher(next.source).matches()) {
                        next = next.next();
                    }
                    if (next != null && (next.type == TokenClassification.QUOT_LITERAL || next.type == TokenClassification.APOS_LITERAL)) {
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

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (null == languageCodePolicy || !languageCodePolicy.disable_builtin_log_detection)) {
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

                    if (token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.NOT_LOG_OUTPUT || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                            token.exceptionClass = matcherException.group(reException_group_class);
                        }
                    }

                    if (token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) token.classification = Token.Classification.LOG_OUTPUT;

                    // message categorisation, dynamic
                    if (token.classification == Token.Classification.EXCEPTION_THROW || token.classification == Token.Classification.LOG_OUTPUT) {
                        if (null != token.next()) {
                            // note token current is a type of string literal.
                            boolean staticLiteral = true;
                            Token current = token.next();
                            while (current != null && current.type == TokenClassification.SOURCE_CODE && Main.reWhitespace.matcher(current.source).matches()) {
                                current = current.next();
                            }
                            if (null != current) {
                                StringBuilder cleaned = new StringBuilder();
                                StringBuilder output = new StringBuilder();
                                boolean abort = false;
                                do {
                                    String sourceCode = null != current.sourceNoErrorCode ? current.sourceNoErrorCode : current.source;
                                    switch (current.type) {
                                        case SOURCE_CODE:
                                            if (sourceCode.indexOf('\n') >= 0) {
                                                if (!reContinuation.matcher(sourceCode).find()) {
                                                    abort = true;
                                                    Matcher matcherIfUnless = reIfUnless.matcher(sourceCode);
                                                    if (matcherIfUnless.find()) {
                                                        sourceCode = sourceCode.substring(0, matcherIfUnless.start());
                                                    }
                                                    if (!Main.reWhitespace.matcher(sourceCode).matches()) {
                                                        staticLiteral = false;
                                                    }
                                                }
                                            } else {
                                                if (!Main.reWhitespace.matcher(sourceCode).matches()) {
                                                    staticLiteral = false;
                                                }
                                            }
                                            break;
                                        case COMMENT_BLOCK:
                                        case CONTENT:
                                        case COMMENT_LINE:
                                            break;
                                        case QUOT_LITERAL:
                                            if (token.next() == current) {
                                                if (sourceCode.contains("#{") ||
                                                        sourceCode.contains("#@") ||
                                                        sourceCode.contains("#$")) {
                                                    staticLiteral = false;
                                                }
                                            }
                                            // falls through to the next case:
                                        case APOS_LITERAL:
                                            cleaned.append(sourceCode);
                                            output.append(sourceCode);
                                            break;
                                    }
                                }
                                while (!abort && null != (current = current.next()));

                                token.staticLiteral = staticLiteral;
                                token.cleanedMessageExpression = cleaned.toString();
                                token.messageExpression = output.toString();
                            }
                            else {
                                token.classification = Token.Classification.NO_MATCH;
                            }
                        } else {
                            token.classification = Token.Classification.NO_MATCH;
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
            if (token.type == TokenClassification.SOURCE_CODE) {
                Matcher matcherMethod = reMethod.matcher(token.source);
                if (matcherMethod.find()) {
                    final String code = matcherMethod.group();
                    //if (reMethodIgnore.matcher(code).find())
                    //    continue;
                    token.classification = Token.Classification.METHOD_SIGNATURE;
                    token.extractedCode = code;
                } else {
                    token.classification = Token.Classification.NO_MATCH;
                }
            } else {
                token.classification = Token.Classification.NO_MATCH;
            }
        }
    }
}
