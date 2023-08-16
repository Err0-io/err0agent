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

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpSourceCodeParse extends SourceCodeParse {

    public PhpSourceCodeParse(final CodePolicy policy)
    {
        super(Language.PHP, policy, policy.adv_php);
        switch (policy.mode) {
            case DEFAULTS:
                reLogger = Pattern.compile("(^|\\s|\\\\|\\$|->)(error_log|(m?_?)*log(ger)?(\\\\|::|->)(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info|fault|notice))\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE);
                break;

            case EASY_CONFIGURATION:
            case ADVANCED_CONFIGURATION:
                reLogger = Pattern.compile("(^|\\s|\\\\|\\$|->)(error_log|" + policy.easyModeObjectPattern() + "(\\\\|::|->)" + policy.easyModeMethodPattern() + ")\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE);
                break;
        }

        final String pattern = policy.mode == CodePolicy.CodePolicyMode.DEFAULTS ? "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info|fault|notice)" : policy.easyModeMethodPattern();
        reLoggerLevel = Pattern.compile("(\\\\|::|->)(" + pattern + ")\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE); // group #2 is our match
    }

    private static Pattern reMethodPerhaps = Pattern.compile("\\)(\\s*:\\s*\\S.*?)?\\s*$");
    private static Pattern reMethod = Pattern.compile("\\s*(([^){};]+?)\\([^)]*?\\)?(\\s*use\\s*\\([^)]+?\\))?(\\s*:\\s*\\S.*?)?)\\s*$");
    private static Pattern reControl = Pattern.compile("(^|\\s+)(for(each)?|if|else(\\s*if)?|do|while|switch|try|catch|finally)(\\(|\\{|\\s|$)", Pattern.MULTILINE);
    private static Pattern reClass = Pattern.compile("\\s*(([^){};]+?\\s+?)?class\\s+(\\S+)[^;{(]+?)\\s*$");
    private Pattern reLogger = null;
    private Pattern reLoggerLevel = null;
    private static Pattern reException = Pattern.compile("throw\\s+new\\s+([^\\s\\(]*)\\s*\\(\\s*$");
    private static int reException_group_class = 1;
    private static Pattern reVariableInString = Pattern.compile("(?<!\\\\)\\$\\S+", Pattern.CASE_INSENSITIVE);

    public static PhpSourceCodeParse lex(final CodePolicy policy, final String sourceCode) {
        int n = 0;
        PhpSourceCodeParse parse = new PhpSourceCodeParse(policy);
        Token currentToken = new Token(n++, null);
        currentToken.type = TokenType.CONTENT;
        int lineNumber = 1;
        currentToken.startLineNumber = lineNumber;
        final char chars[] = sourceCode.toCharArray();
        for (int i = 0, l = chars.length; i < l; ++i) {
            int depth = currentToken.depth;
            final char ch = chars[i];
            if (ch == '\n') {
                ++lineNumber;
            }
            switch (currentToken.type) {
                case CONTENT:
                    if (ch == '<' && i < l - 1 && chars[i + 1] == '?') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[i + 1]);
                        ++i;
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case SOURCE_CODE:
                    if (ch == '?' && i < l-1 && chars[i+1] == '>') {
                        currentToken.sourceCode.append(ch);
                        currentToken.sourceCode.append(chars[i+1]);
                        ++i;
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.CONTENT;
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '{') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = depth + 1;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '}') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = depth - 1;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == ';') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.SOURCE_CODE;
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\'') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.APOS_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\"') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenType.QUOT_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (i < l - 1 && ch == '/') {
                        final char ch2 = chars[i+1];
                        if (ch2 == '*') {
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenType.COMMENT_BLOCK;
                            currentToken.sourceCode.append(ch);
                            currentToken.sourceCode.append(ch2);
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                            ++i;
                        } else if (ch2 == '/') {
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenType.COMMENT_LINE;
                            currentToken.sourceCode.append(ch);
                            currentToken.sourceCode.append(ch2);
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                            ++i;
                        } else {
                            currentToken.sourceCode.append(ch);
                        }
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
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                case COMMENT_BLOCK:
                    if (ch == '*' && i < l-1) {
                        final char ch2 = chars[i+1];
                        if (ch2 == '/') {
                            currentToken.sourceCode.append(ch);
                            currentToken.sourceCode.append(ch2);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenType.SOURCE_CODE;
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                            ++i;
                        } else {
                            currentToken.sourceCode.append(ch);
                        }
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
                        currentToken.depth = depth;
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
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (ch == '\\') {
                        currentToken.sourceCode.append(ch);
                        final char ch2 = chars[++i];
                        currentToken.sourceCode.append(ch2);
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
            }
        }
        parse.tokenList.add(currentToken.finish(lineNumber));
        return parse;
    }

    @Override
    public boolean couldContainErrorNumber(Token token) {
        return token.type == TokenType.APOS_LITERAL || token.type == TokenType.QUOT_LITERAL;
    }

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case APOS_LITERAL:
                case QUOT_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_php().matcher(token.source);
                    if (matcherErrorNumber.find()) {
                        token.classification = Token.Classification.ERROR_NUMBER;
                        long errorOrdinal = Long.parseLong(matcherErrorNumber.group(2));
                        if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                            if (globalState.store(errorOrdinal, stateItem, token)) {
                                token.keepErrorCode = true;
                                token.errorOrdinal = errorOrdinal;
                                token.sourceNoErrorCode = token.source.substring(0, 1) + token.source.substring(matcherErrorNumber.end());
                            } else {
                                token.sourceNoErrorCode = token.source = token.source.substring(0,1) + token.source.substring(matcherErrorNumber.end());
                            }
                        } else {
                            token.sourceNoErrorCode = token.source = token.source.substring(0,1) + token.source.substring(matcherErrorNumber.end());
                        }
                    } else if (policy.getCodePolicy().getEnablePlaceholder()) {
                        Matcher matcherPlaceholder = policy.getReErrorNumber_php_placeholder().matcher(token.source);
                        if (matcherPlaceholder.matches()) {
                            token.classification = Token.Classification.PLACEHOLDER;
                            String number = matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_number_group);
                            if (null != number && ! "".equals(number)) {
                                long errorOrdinal = Long.parseLong(number);
                                if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                                    if (globalState.store(errorOrdinal, stateItem, token)) {
                                        token.keepErrorCode = true;
                                        token.errorOrdinal = errorOrdinal;
                                        token.sourceNoErrorCode = matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group);
                                    } else {
                                        token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group);
                                    }
                                } else {
                                    token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group);
                                }
                            } else {
                                token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_php_placeholder_open_close_group);
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
                    if (next != null && (next.type == TokenType.QUOT_LITERAL || next.type == TokenType.APOS_LITERAL)) {
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
                                token.loggerLevel = matcherLoggerLevel.group(2);
                            }
                        }
                    }

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.NOT_LOG_OUTPUT || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (!codePolicy.getDisableExceptions())) {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                            token.exceptionClass = matcherException.group(reException_group_class);
                            if (token.exceptionClass.startsWith("\\")) {
                                token.exceptionClass = token.exceptionClass.substring(1);
                            }
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
                                        if (current.type == TokenType.QUOT_LITERAL) {
                                            if (reVariableInString.matcher(sourceCode).find()) {
                                                staticLiteral = false;
                                            }
                                        }
                                        cleaned.append(sourceCode);
                                        output.append(sourceCode);
                                        break;
                                }
                                if (bracketDepth < 1) {
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

    private String codeWithAnnotations(int n, int startIndex, String code) {
        // Go backwards from matcherMethod.start(1) through previous blocks
        boolean abort = false;
        StringBuilder backwardsCode = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        for (int i = code.length() - 1; i >= 0; --i) {
            char ch = code.charAt(i);
            if (ch == ')' || ch == '}') {
                stack.push(ch);
            } else if (! stack.empty() && (ch == '(' || ch == '{')) {
                stack.pop();
            }
        }
        for (int i = n, j = startIndex; !abort && i > 0; j = tokenList.get(--i).source.length() - 1) {
            Token currentToken = tokenList.get(i);
            if (currentToken.type == TokenType.COMMENT_BLOCK || currentToken.type == TokenType.COMMENT_LINE || currentToken.type == TokenType.CONTENT)
                continue;
            for (; !abort && j >= 0; --j) {
                char ch = currentToken.source.charAt(j);
                if (currentToken.type == TokenType.SOURCE_CODE) {
                    if (stack.empty()) {
                        if (ch == ',' || ch == ';' || ch == '{' || ch == '(' || ch == '}') {
                            abort = true;
                            break;
                        } else if (ch == ')') {
                            stack.push(ch);
                        }
                        backwardsCode.append(ch);
                    } else {
                        if (ch == ')' || ch == '}') {
                            stack.push(ch);
                        } else if (ch == '(' || ch == '{') {
                            stack.pop();
                        }
                        backwardsCode.append(ch);
                    }
                } else {
                    backwardsCode.append(ch);
                }
            }
        }

        backwardsCode.reverse();
        backwardsCode.append(code);

        return backwardsCode.toString();
    }

    @Override
    public void classifyForCallStack(Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET || token.classification == Token.Classification.NOT_FULLY_CLASSIFIED) {
            if (token.type == TokenType.SOURCE_CODE) {
                boolean foundMethod = false;
                Matcher matcherMethodPerhaps = reMethodPerhaps.matcher(token.source);
                if (matcherMethodPerhaps.find()) {
                    String codeBlock = codeWithAnnotations(token.n, matcherMethodPerhaps.end() - 1, "");
                    Matcher matcherMethod = reMethod.matcher(codeBlock);
                    if (matcherMethod.find()) {
                        String code = matcherMethod.group(1);
                        if (code.startsWith("<?php")) {
                            code = code.substring(5).trim();
                        } else if (code.startsWith("<?")) {
                            code = code.substring(2).trim();
                        }
                        //if (reMethodIgnore.matcher(code).find())
                        //    continue;
                        token.classification = Token.Classification.METHOD_SIGNATURE;
                        token.extractedCode = code;
                        foundMethod = true;
                        if (reControl.matcher(token.extractedCode).find()) {
                            token.classification = Token.Classification.CONTROL_SIGNATURE;
                        }
                    }
                }

                if (!foundMethod) {
                    Matcher matcherClass = reClass.matcher(token.source);
                    if (matcherClass.find()) {
                        token.classification = Token.Classification.CLASS_SIGNATURE;
                        token.extractedCode = matcherClass.group(1);
                    } else {
                        //Matcher matcherLambda = reLambda.matcher(token.source);
                        //if (matcherLambda.find()) {
                        //    token.classification = Token.Classification.LAMBDA_SIGNATURE;
                        //    token.extractedCode = matcherLambda.group(1);
                        //} else {
                            token.classification = Token.Classification.NO_MATCH;
                        //}
                    }
                }
            } else {
                token.classification = Token.Classification.NO_MATCH;
            }
        }
    }
}
