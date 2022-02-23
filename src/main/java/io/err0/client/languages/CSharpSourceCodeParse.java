package io.err0.client.languages;

import com.google.gson.JsonArray;
import io.err0.client.Main;
import io.err0.client.core.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSharpSourceCodeParse extends SourceCodeParse {

    public CSharpSourceCodeParse(final CodePolicy policy) {
        super(Language.C_SHARP, policy, policy.adv_csharp);
        switch (policy.mode) {
            case DEFAULTS:
                reLogger = Pattern.compile("(m?_?)*log(ger)?\\.(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)(<[^>]+>)?\\s*\\([^\")]*\\s*$", Pattern.CASE_INSENSITIVE);
                break;

            case EASY_CONFIGURATION:
            case ADVANCED_CONFIGURATION:
                reLogger = Pattern.compile(policy.easyModeObjectPattern() + "\\." + policy.easyModeMethodPattern() + "(<[^>]+>)?\\s*\\([^\")]*\\s*$", Pattern.CASE_INSENSITIVE);
                break;
        }
    }

    private static Pattern reMethodPerhaps = Pattern.compile("\\)\\s*$");
    private static Pattern reMethod = Pattern.compile("\\s*(([^{};]+?)\\([^)]*?\\)(\\s+throws\\s+[^;{(]+?)?)\\s*$");
    private static Pattern reLambda = Pattern.compile("\\s*(([^){\\[\\]};,]+?)\\([^)]*?\\)\\s+=>\\s*)\\s*$");
    private static Pattern reClass = Pattern.compile("\\s*(([^){\\[\\]};]+?)\\s+class\\s+(\\S+)[^;{(]+?)\\s*$");
    private static Pattern reMethodIgnore = Pattern.compile("(\\s+|^\\s*)(catch|if|do|while|switch|for)\\s+", Pattern.MULTILINE);
    //private static Pattern reErrorNumber = Pattern.compile("^\"\\[ERR-(\\d+)\\]\\s+");
    private Pattern reLogger = null;
    private static Pattern reException = Pattern.compile("throw\\s+new\\s+([^\\s(]*)\\s*\\(\\s*$");
    private static int reException_group_class = 1;
    private static Pattern rePreprocessor = Pattern.compile("^(\\s*#([^#\r\n]+))", Pattern.MULTILINE);

    public static CSharpSourceCodeParse lex(final CodePolicy policy, final String sourceCode) {
        int n = 0;
        CSharpSourceCodeParse parse = new CSharpSourceCodeParse(policy);
        Token currentToken = new Token(n++, null);
        currentToken.type = TokenClassification.SOURCE_CODE;
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
                case SOURCE_CODE:
                    if (ch == '{') {
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
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.QUOT_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = depth;
                        currentToken.startLineNumber = lineNumber;
                    } else if (i < l - 1 && ch == '/') {
                        final char ch2 = chars[i+1];
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
            }
        }
        parse.tokenList.add(currentToken.finish(lineNumber));
        return parse;
    }

    @Override
    public boolean couldContainErrorNumber(Token token) {
        return token.type == TokenClassification.QUOT_LITERAL;
    }

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case QUOT_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_cs().matcher(token.source);
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
                    if (next != null && (next.type == TokenClassification.QUOT_LITERAL)) {
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
                                    lineOfCode = GsonHelper.getAsString(lineArray.get(0).getAsJsonObject(), "c", null);
                                }
                            }
                            token.classification = languageCodePolicy.classify(lineOfCode, stringLiteral);
                        }
                    } else {
                        token.classification = Token.Classification.NO_MATCH;
                    }

                    if ((token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) && (null == languageCodePolicy || !languageCodePolicy.disable_builtin_log_detection)) {
                        Matcher matcherLogger = reLogger.matcher(token.source);
                        if (matcherLogger.find()) {
                            token.classification = Token.Classification.LOG_OUTPUT;
                            // TODO: extract canonical log level meta data
                        }
                    }

                    if (token.classification == Token.Classification.NOT_FULLY_CLASSIFIED || token.classification == Token.Classification.NOT_LOG_OUTPUT || token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION)
                    {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                            token.exceptionClass = matcherException.group(reException_group_class);
                        }
                    }

                    if (token.classification == Token.Classification.MAYBE_LOG_OR_EXCEPTION) token.classification = Token.Classification.LOG_OUTPUT;
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
                        if (/*ch == ',' || */ch == ';' || ch == '{' || ch == '(' || ch == '}') {
                            abort = true;
                            break;
                        } else if (ch == ']' || ch == ')') {
                            stack.push(ch);
                            useBackwardsCode = true;
                        }
                        backwardsCode.append(ch);
                    } else {
                        if (ch == ')' || ch == '}' || ch == ']') {
                            stack.push(ch);
                        } else if (ch == '(' || ch == '{' || ch == '[') {
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
                boolean foundMethod = false;
                Matcher matcherMethodPerhaps = reMethodPerhaps.matcher(token.source);
                if (matcherMethodPerhaps.find()) {
                    String codeBlock = codeWithAnnotations(token.n, matcherMethodPerhaps.end() - 1, "");
                    Matcher matcherMethod = reMethod.matcher(codeBlock);
                    if (matcherMethod.find()) {
                        String code = matcherMethod.group(1);

                        Matcher mPreprocessor = rePreprocessor.matcher(code);
                        while (mPreprocessor.find()) { // maybe it's #endregion \r\n #region foo \r\n your code...
                            code = code.substring(mPreprocessor.end());
                            mPreprocessor = rePreprocessor.matcher(code);
                        }

                        //if (reMethodIgnore.matcher(code).find())
                        //    continue;
                        token.classification = Token.Classification.METHOD_SIGNATURE;
                        token.extractedCode = code;
                        foundMethod = true;
                    }
                }
                if (!foundMethod) {
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
