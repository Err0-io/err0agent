package io.err0.client.languages;

import io.err0.client.core.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypescriptSourceCodeParse extends SourceCodeParse {

    public TypescriptSourceCodeParse()
    {
        super(Language.TYPESCRIPT);
    }

    private static Pattern reMethod = Pattern.compile("\\s*(([^){};]+?)\\([^)]*?\\)(:[^;{(]+?)?)\\s*$");
    private static Pattern reLambda = Pattern.compile("\\s*(([^){};,]+?)\\([^)]*?\\)\\s+=>\\s*)\\s*$");
    private static Pattern reClass = Pattern.compile("\\s*(([^){};]+?)\\s+class\\s+(\\S+)[^;{(]+?)\\s*$");
    private static Pattern reMethodIgnore = Pattern.compile("(\\s+|^\\s*)(catch|if|do|while|switch|for)\\s+", Pattern.MULTILINE);
    //private static Pattern reErrorNumber = Pattern.compile("^(`|'|\")\\[ERR-(\\d+)\\]\\s+");
    private static Pattern reLogger = Pattern.compile("(_?log(ger)?|console)\\.(error|warn|log|info)\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE);
    private static Pattern reException = Pattern.compile("throw\\s+(new\\s+)?([^\\s(]*)\\s*\\(\\s*$");
    private static int reException_group_class = 2;
    
    public static TypescriptSourceCodeParse lex(final String sourceCode) {
        int n = 0;
        TypescriptSourceCodeParse parse = new TypescriptSourceCodeParse();
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
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.SOURCE_CODE;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = depth - 1;
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
                    } else if (ch == '`') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++, currentToken);
                        currentToken.type = TokenClassification.BACKTICK_LITERAL;
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
                case BACKTICK_LITERAL:
                    if (ch == '`') {
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
        return token.type == TokenClassification.APOS_LITERAL || token.type == TokenClassification.BACKTICK_LITERAL || token.type == TokenClassification.QUOT_LITERAL;
    }

    @Override
    public void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token) {
        if (token.classification == Token.Classification.NOT_CLASSIFIED_YET) {
            switch (token.type) {
                case APOS_LITERAL:
                case BACKTICK_LITERAL:
                case QUOT_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_ts().matcher(token.source);
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
                    } else {
                        token.classification = Token.Classification.POTENTIAL_ERROR_NUMBER;
                        token.sourceNoErrorCode = token.source;
                    }
                }
                break;
                case SOURCE_CODE:
                {
                    Matcher matcherLogger = reLogger.matcher(token.source);
                    if (matcherLogger.find()) {
                        token.classification = Token.Classification.LOG_OUTPUT;
                        // TODO: extract canonical log level meta data
                    } else {
                        Matcher matcherException = reException.matcher(token.source);
                        if (matcherException.find()) {
                            token.classification = Token.Classification.EXCEPTION_THROW;
                            token.exceptionClass = matcherException.group(reException_group_class);
                        } else {
                            token.classification = Token.Classification.NOT_FULLY_CLASSIFIED;
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
                    final String code = matcherMethod.group(1);
                    //if (reMethodIgnore.matcher(code).find())
                    //    continue;
                    token.classification = Token.Classification.METHOD_SIGNATURE;
                    token.extractedCode = code;
                } else {
                    Matcher matcherClass = reClass.matcher(token.source);
                    if (matcherClass.find()) {
                        token.classification = Token.Classification.CLASS_SIGNATURE;
                        token.extractedCode = matcherClass.group(1);
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
