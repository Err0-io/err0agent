package io.err0.client.core;

import java.util.ArrayList;

import static io.err0.client.Main.reWhitespace;

public class CurlyCallStackLogic implements CallStackLogic {
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
            }
        }

        return callStackReversed;
    }
}
