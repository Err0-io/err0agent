package io.err0.client.core;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.err0.client.Main.reWhitespace;

public class ObjectiveCCallStackLogic implements CallStackLogic {

    final Pattern reImplementation = Pattern.compile("^@implementation\\s+(\\S+)$");

    @Override
    public ArrayList<MethodData> reversed(final int j, final SourceCodeParse parse, final Token currentToken) {
        ArrayList<MethodData> callStackReversed = new ArrayList<>();
        for (int k = j - 1, depth = currentToken.depth; k >= 0; --k) {
            final Token tok = parse.tokenList.get(k);
            if (tok.type == TokenType.SOURCE_CODE) {
                if (reWhitespace.matcher(tok.source).matches()) {
                    continue; // skip whitespace
                }
            }
            if (tok.depth >= depth) continue;
            if (tok.type == TokenType.CONTENT) continue;
            depth = tok.depth;
            Token nextTok = null;
            for (int x = k + 1; x < j; ++x) {
                nextTok = parse.tokenList.get(x);
                if (nextTok.type == TokenType.CONTENT) continue;
                break;
            }

            if (tok.type == TokenType.SOURCE_CODE && null != nextTok && nextTok.type == TokenType.SOURCE_CODE && tok.depth < nextTok.depth) {
                parse.classifyForCallStack(tok);

                if (tok.classification == Token.Classification.CLASS_SIGNATURE ||
                        tok.classification == Token.Classification.METHOD_SIGNATURE ||
                        tok.classification == Token.Classification.LAMBDA_SIGNATURE ||
                        tok.classification == Token.Classification.CONTROL_SIGNATURE
                ) {
                    callStackReversed.add(new MethodData(tok.lastLineNumber, tok.extractedCode, tok.classification));
                }

                if (tok.classification == Token.Classification.METHOD_SIGNATURE) {
                    MethodData implementation = null;
                    for (int i = 0; i < tok.n; ++i) {
                        Token other = parse.tokenList.get(i);
                        if (other.type == TokenType.SOURCE_CODE) {
                            if (other.source.startsWith("@")) {
                                Matcher matcherImplementation = reImplementation.matcher(other.source);
                                if (matcherImplementation.find()) {
                                    implementation = new MethodData(other.lastLineNumber, other.source, Token.Classification.CLASS_SIGNATURE);
                                } else {
                                    implementation = null;
                                }
                            }
                        }
                    }
                    if (null != implementation) {
                        callStackReversed.add(implementation);
                    }
                }
            }
        }

        return callStackReversed;
    }
}
