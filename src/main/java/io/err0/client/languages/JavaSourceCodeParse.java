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

public class JavaSourceCodeParse extends SourceCodeParse {

    public JavaSourceCodeParse(final CodePolicy policy)
    {
        super(Language.JAVA, policy, policy.adv_java);
        switch (policy.mode) {
            case DEFAULTS:
                reLogger = Pattern.compile("(^|\\s+)(m?_?)*log(ger)?\\.(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                reFluentSlf4jConfirm = Pattern.compile("^\\s*(m?_?)*log(ger)?\\.(atFatal|atError|atWarn|atInfo)\\(\\)\\.", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); // atDebug|atTrace
                break;

            case EASY_CONFIGURATION:
            case ADVANCED_CONFIGURATION:
                reLogger = Pattern.compile("(^|\\s+)" + policy.easyModeObjectPattern() + "\\." + policy.easyModeMethodPattern() + "\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                reFluentSlf4jConfirm = Pattern.compile("^\\s*" + policy.easyModeObjectPattern() + "\\.at" + policy.easyModeMethodPattern() + "\\(\\)\\.", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                break;
        }

        final String pattern = policy.mode == CodePolicy.CodePolicyMode.DEFAULTS ? "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)" : policy.easyModeMethodPattern();
        reLoggerLevel = Pattern.compile("\\.(" + pattern + ")\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); // group #1 is the level
        reFluentSlf4jLevel = Pattern.compile("\\.at(" + pattern + ")\\(\\)\\.", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); // group #1 is the level
    }

    private static Pattern reMethod = Pattern.compile("\\s*(([^){};]+?)\\(.*?\\)(\\s+throws\\s+[^;{(]+?)?)\\s*$");
    private static Pattern reLambda = Pattern.compile("\\s*(([^){};,=]+?)\\([^)]*?\\)\\s+->\\s*)\\s*$");
    private static Pattern reClass = Pattern.compile("\\s*(([^){};]*\\s+)?class\\s+(\\S+)[^;{(]+?)\\s*$");
    private Pattern reLogger = null;
    private Pattern reLoggerLevel = null;
    private static Pattern reFluentSlf4j = Pattern.compile("\\.log\\s*\\(\\s*$");
    private Pattern reFluentSlf4jConfirm = null;
    private Pattern reFluentSlf4jLevel = null;
    private static Pattern reException = Pattern.compile("throw\\s+new\\s+([^\\s\\(]*)\\s*\\(\\s*$");
    private static int reException_group_class = 1;

    public static JavaSourceCodeParse lex(final CodePolicy policy, final String sourceCode) {
        int n = 0;
        JavaSourceCodeParse parse = new JavaSourceCodeParse(policy);
        Token currentToken = new Token(n++, null);
        currentToken.type = TokenClassification.SOURCE_CODE;
        int lineNumber = 1;
        currentToken.startLineNumber = lineNumber;
        final char chars[] = sourceCode.toCharArray();
        boolean inAnnotation = false;
        int annotationBracketDepth = 0;
        for (int i = 0, l = chars.length; i < l; ++i) {
            int depth = currentToken.depth;
            final char ch = chars[i];
            if (ch == '\n') {
                ++lineNumber;
            }
            switch (currentToken.type) {
                case SOURCE_CODE:
                    if (inAnnotation) {
                        if (annotationBracketDepth > 0) {
                            if (ch == ')') {
                                --annotationBracketDepth;
                            }
                            currentToken.sourceCode.append(ch);
                        } else {
                            if (ch == '(') {
                                ++annotationBracketDepth;
                            }
                            if (Character.isWhitespace(ch)) {
                                inAnnotation = false;
                            }
                            currentToken.sourceCode.append(ch);
                        }
                    } else {
                        if (ch == '@') {
                            inAnnotation = true;
                            annotationBracketDepth = 0;
                            currentToken.sourceCode.append(ch);
                        } else if (ch == '{') {
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.sourceCode.append(ch);
                            currentToken.depth = depth + 1;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == '}') {
                            currentToken.sourceCode.append(ch);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.depth = depth - 1;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == ';') {
                            currentToken.sourceCode.append(ch);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == '\'') {
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.APOS_LITERAL;
                            currentToken.sourceCode.append(ch);
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                        } else if (ch == '\"') {
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            if (i < l - 2 && chars[i + 1] == '\"' && chars[i + 2] == '\"') {
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.QUOT3_LITERAL;
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(chars[++i]);
                                currentToken.sourceCode.append(chars[++i]);
                                currentToken.depth = depth;
                                currentToken.startLineNumber = lineNumber;
                            } else {
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.QUOT_LITERAL;
                                currentToken.sourceCode.append(ch);
                                currentToken.depth = depth;
                                currentToken.startLineNumber = lineNumber;
                            }
                        } else if (i < l - 1 && ch == '/') {
                            final char ch2 = chars[i + 1];
                            if (ch2 == '*') {
                                parse.tokenList.add(currentToken.finish(lineNumber));
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.COMMENT_BLOCK;
                                currentToken.sourceCode.append(ch);
                                currentToken.sourceCode.append(ch2);
                                currentToken.depth = depth;
                                currentToken.startLineNumber = lineNumber;
                                ++i;
                            } else if (ch2 == '/') {
                                parse.tokenList.add(currentToken.finish(lineNumber));
                                currentToken = new Token(n++, currentToken);
                                currentToken.type = TokenClassification.COMMENT_LINE;
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
                    }
                    break;
                case COMMENT_LINE:
                    if (ch == '\n') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.SOURCE_CODE;
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
                            currentToken.type = TokenClassification.SOURCE_CODE;
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
                        currentToken.type = TokenClassification.SOURCE_CODE;
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
                        currentToken.type = TokenClassification.SOURCE_CODE;
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
                case QUOT3_LITERAL:
                    if (ch == '\"') {
                        if (i < l-2 && chars[i+1] == '\"' && chars[i+2] == '\"') {
                            currentToken.sourceCode.append(ch);
                            currentToken.sourceCode.append(chars[++i]);
                            currentToken.sourceCode.append(chars[++i]);
                            parse.tokenList.add(currentToken.finish(lineNumber));
                            currentToken = new Token(n++, currentToken);
                            currentToken.type = TokenClassification.SOURCE_CODE;
                            currentToken.depth = depth;
                            currentToken.startLineNumber = lineNumber;
                        } else {
                            currentToken.sourceCode.append(ch);
                        }
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
        return token.type == TokenClassification.QUOT_LITERAL || token.type == TokenClassification.QUOT3_LITERAL;
    }

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case QUOT_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_java().matcher(token.source);
                    if (matcherErrorNumber.find()) {
                        token.classification = Token.Classification.ERROR_NUMBER;
                        long errorOrdinal = Long.parseLong(matcherErrorNumber.group(1));
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
                        Matcher matcherPlaceholder = policy.getReErrorNumber_java_placeholder().matcher(token.source);
                        if (matcherPlaceholder.matches()) {
                            token.classification = Token.Classification.PLACEHOLDER;
                            String number = matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_number_group);
                            if (null != number && ! "".equals(number)) {
                                long errorOrdinal = Long.parseLong(number);
                                if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                                    if (globalState.store(errorOrdinal, stateItem, token)) {
                                        token.keepErrorCode = true;
                                        token.errorOrdinal = errorOrdinal;
                                        token.sourceNoErrorCode = matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group);
                                    } else {
                                        token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group);
                                    }
                                } else {
                                    token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group);
                                }
                            } else {
                                token.sourceNoErrorCode = token.source = matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group) + matcherPlaceholder.group(policy.reErrorNumber_java_placeholder_open_close_group);
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
                case QUOT3_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_java_textblocks().matcher(token.source);
                    if (matcherErrorNumber.find()) {
                        token.classification = Token.Classification.ERROR_NUMBER;
                        long errorOrdinal = Long.parseLong(matcherErrorNumber.group(2));
                        if (apiProvider.validErrorNumber(policy, errorOrdinal)) {
                            if (globalState.store(errorOrdinal, stateItem, token)) {
                                token.keepErrorCode = true;
                                token.errorOrdinal = errorOrdinal;
                                token.sourceNoErrorCode = "\"\"\"" + matcherErrorNumber.group(1) + token.source.substring(matcherErrorNumber.end());
                            } else {
                                token.sourceNoErrorCode = token.source = "\"\"\"" + matcherErrorNumber.group(1) + token.source.substring(matcherErrorNumber.end());
                            }
                        } else {
                            token.sourceNoErrorCode = token.source = "\"\"\"" + matcherErrorNumber.group(1) + token.source.substring(matcherErrorNumber.end());
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
                    if (next != null && (next.type == TokenClassification.QUOT_LITERAL || next.type == TokenClassification.QUOT3_LITERAL)) {
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
                        } else {
                            boolean sl4fjMatch = false;
                            Matcher matcherFluentSlf4j = reFluentSlf4j.matcher(token.source);
                            if (matcherFluentSlf4j.find()) {
                                final String scanBackwards = codeWithAnnotations(token.n, matcherFluentSlf4j.start(), matcherFluentSlf4j.group());
                                if (reFluentSlf4jConfirm.matcher(scanBackwards).find()) {
                                    sl4fjMatch = true;
                                    token.classification = Token.Classification.LOG_OUTPUT;
                                    // extract canonical log level meta data
                                    Matcher matcherLoggerLevel = reFluentSlf4jLevel.matcher(token.source);
                                    if (matcherLoggerLevel.find()) {
                                        token.loggerLevel = matcherLoggerLevel.group(1);
                                    }
                                }
                            }
                        }
                    }

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.NOT_LOG_OUTPUT || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (!codePolicy.getDisableExceptions())) {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                            token.exceptionClass = matcherException.group(reException_group_class);
                        }
                    }

                    if (token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION && !codePolicy.getDisableLogs()) token.classification = Token.Classification.LOG_OUTPUT;

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
                                            if (!(Character.isWhitespace(ch) || ch == '+')) { // string concatenation
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
        boolean useBackwardsCode = false;
        boolean abort = false;
        StringBuilder backwardsCode = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        for (int i = n, j = startIndex; !abort && i > 0; j = tokenList.get(--i).source.length() - 1) {
            Token currentToken = tokenList.get(i);
            if (currentToken.type == TokenClassification.COMMENT_BLOCK || currentToken.type == TokenClassification.COMMENT_LINE || currentToken.type == TokenClassification.CONTENT)
                continue;
            for (; !abort && j >= 0; --j) {
                char ch = currentToken.source.charAt(j);
                if (currentToken.type == TokenClassification.SOURCE_CODE) {
                    if (stack.empty()) {
                        if (ch == ',' || ch == ';' || ch == '{' || ch == '(' || ch == '}') {
                            abort = true;
                            break;
                        } else if (ch == ')') {
                            stack.push(ch);
                            useBackwardsCode = true;
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

        if (useBackwardsCode) {
            backwardsCode.reverse();
            backwardsCode.append(code);
            code = backwardsCode.toString();
        }

        return code;
    }

    @Override
    public void classifyForCallStack(Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET || token.classification == Token.Classification.NOT_FULLY_CLASSIFIED) {
            if (token.type == TokenClassification.SOURCE_CODE) {
                Matcher matcherMethod = reMethod.matcher(token.source);
                if (matcherMethod.find()) {
                    String code = matcherMethod.group(1);
                    //if (reMethodIgnore.matcher(code).find())
                    //    continue;
                    token.classification = Token.Classification.METHOD_SIGNATURE;
                    token.extractedCode = codeWithAnnotations(token.n, matcherMethod.start(1) - 1, code);
                } else {
                    Matcher matcherClass = reClass.matcher(token.source);
                    if (matcherClass.find()) {
                        token.classification = Token.Classification.CLASS_SIGNATURE;
                        token.extractedCode = codeWithAnnotations(token.n, matcherClass.start(1) - 1, matcherClass.group(1));
                    } else {
                        Matcher matcherLambda = reLambda.matcher(token.source);
                        if (matcherLambda.find()) {
                            token.classification = Token.Classification.LAMBDA_SIGNATURE;
                            token.extractedCode = matcherLambda.group(1);
                        } else {
                            token.classification = Token.Classification.NO_MATCH;
                        }
                    }
                }
            } else {
                token.classification = Token.Classification.NO_MATCH;
            }
        }
    }
}
