package io.err0.client.languages;

import io.err0.client.core.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonSourceCodeParse extends SourceCodeParse {

    private static Pattern reMethod = Pattern.compile("^(\\s*)(def|class|if|for|while|except)\\s+.*$");
    private static Pattern reLogger = Pattern.compile("(^|\\s+)\\S*logger\\.(error|warning|info)\\s*\\(\\s*$", Pattern.CASE_INSENSITIVE);
    private static Pattern reException = Pattern.compile("(^|\\s+)raise\\s([^\\s\\(]*)\\s*\\(*.+$");
    private static int reException_group_class = 2;

    public static PythonSourceCodeParse lex(final String sourceCode) {
        int n = 0;
        PythonSourceCodeParse parse = new PythonSourceCodeParse();
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
                        if (i+1<l) {
                            final char ch1 = chars[i+1];
                            if (ch1 == '\"') {
                                if (i+2<l) {
                                    final char ch2 = chars[i+2];
                                    if (ch2 == '\"') {
                                        // comment literal """ <<comment>> """
                                        parse.tokenList.add(currentToken.finish(lineNumber));
                                        currentToken = new Token(n++, currentToken);
                                        currentToken.type = TokenClassification.QUOT3_LITERAL;
                                        currentToken.sourceCode.append(ch);
                                        currentToken.sourceCode.append(ch1);
                                        currentToken.sourceCode.append(ch2);
                                        currentToken.depth = indentNumber;
                                        currentToken.startLineNumber = lineNumber;
                                        ++i;
                                        ++i;

                                        break;
                                    }
                                }
                            }
                        }

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
                    } /*else if (ch == '`') {
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++);
                        currentToken.type = TokenClassification.BACKTICK_LITERAL;
                        currentToken.sourceCode.append(ch);
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                        currentToken.startIndentNumber = indentNumber;
                    }*/ else {
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
                case QUOT3_LITERAL:
                    if (ch == '\"') {
                        if (i+1<l) {
                            final char ch1 = chars[i+1];
                            if (ch1 == '\"') {
                                if (i+2<l) {
                                    final char ch2 = chars[i+2];
                                    if (ch2 == '\"') {
                                        // comment literal """ <<comment>> """
                                        currentToken.sourceCode.append(ch);
                                        currentToken.sourceCode.append(ch1);
                                        currentToken.sourceCode.append(ch2);
                                        parse.tokenList.add(currentToken.finish(lineNumber));
                                        currentToken = new Token(n++, currentToken);
                                        currentToken.type = TokenClassification.SOURCE_CODE;
                                        currentToken.depth = indentNumber;
                                        currentToken.startLineNumber = lineNumber;
                                        i+=2;

                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // behaves like any string
                    if (ch == '\\') {
                        currentToken.sourceCode.append(ch);
                        final char ch2 = chars[++i];
                        currentToken.sourceCode.append(ch2);
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
                    break;
                    /*
                case BACKTICK_LITERAL:
                    if (ch == '`') {
                        currentToken.sourceCode.append(ch);
                        parse.tokenList.add(currentToken.finish(lineNumber));
                        currentToken = new Token(n++);
                        currentToken.type = TokenClassification.SOURCE_CODE;
                        currentToken.depth = indentNumber;
                        currentToken.startLineNumber = lineNumber;
                        currentToken.startIndentNumber = indentNumber;
                    } else {
                        currentToken.sourceCode.append(ch);
                    }
                    break;
                    */
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
                case QUOT_LITERAL:
                case QUOT3_LITERAL:
                {
                    // 1) Strip '[ERR-nnnnnn] ' from string literals for re-injection
                    Matcher matcherErrorNumber = policy.getReErrorNumber_py().matcher(token.source);
                    if (matcherErrorNumber.find()) {
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
