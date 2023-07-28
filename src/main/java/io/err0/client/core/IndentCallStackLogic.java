package io.err0.client.core;

import java.util.ArrayList;

import static io.err0.client.Main.reWhitespace;

public class IndentCallStackLogic implements CallStackLogic {
    @Override
    public ArrayList<MethodData> reversed(final int j, final SourceCodeParse parse, final Token currentToken) {
        ArrayList<MethodData> callStackReversed = new ArrayList<>();
        for (int k = j - 1, depth = Integer.MAX_VALUE; k >= 0; --k) {
            final Token tok = parse.tokenList.get(k);
            if (reWhitespace.matcher(tok.source).matches()) {
                continue; // skip whitespace
            }
            if (tok.type != TokenType.SOURCE_CODE) continue;
            if (tok.depth >= depth) continue;
            depth = tok.depth;

            parse.classifyForCallStack(tok);

            if (tok.classification == Token.Classification.CLASS_SIGNATURE ||
                    tok.classification == Token.Classification.METHOD_SIGNATURE ||
                    tok.classification == Token.Classification.LAMBDA_SIGNATURE
            ) {
                callStackReversed.add(new MethodData(tok.lastLineNumber, tok.extractedCode));
                if (depth == 0) break;
            }

        }

        return callStackReversed;
    }
}
