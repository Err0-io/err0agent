package io.err0.client.core;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static io.err0.client.Main.reWhitespace;

public class IndentCallStackLogic implements CallStackLogic {
    @Override
    public ArrayList<MethodData> reversed(final int j, final SourceCodeParse parse, final Token currentToken) {
        Pattern lineContinuationPattern = null;
        switch (parse.language) {
            case PYTHON:
                lineContinuationPattern = Pattern.compile("\\):\\s*$");
                break;
            case RUBY:
                lineContinuationPattern = Pattern.compile("[+\\\\]\\s*$");
                break;
            case LUA:
                lineContinuationPattern = Pattern.compile("\\)\\s*$");
                break;
            default:
                throw new RuntimeException("[AGENT-000115] Unexpected language");
        }

        ArrayList<MethodData> callStackReversed = new ArrayList<>();
        for (int k = j - 1, depth = Integer.MAX_VALUE; k >= 0; --k) {
            Token tok = parse.tokenList.get(k);
            if (reWhitespace.matcher(tok.source).matches()) {
                continue; // skip whitespace
            }
            if (tok.type != TokenType.SOURCE_CODE) continue;
            if (tok.depth >= depth) continue;
            depth = tok.depth;

            if (k > 0 && lineContinuationPattern.matcher(tok.source).find()) {
                int save = k;
                while (--k > 0) {
                    Token t = parse.tokenList.get(k);
                    if (t.type != TokenType.SOURCE_CODE) continue;
                    if (reWhitespace.matcher(t.source).matches()) {
                        k = save;
                        break;
                    }
                    if (t.depth > depth) continue;
                    if (t.depth < depth) {
                        k = save;
                        break;
                    }
                    // t.depth == depth
                    tok = t;
                    break;
                }
            }

            parse.classifyForCallStack(tok);

            if (tok.classification == Token.Classification.CLASS_SIGNATURE ||
                    tok.classification == Token.Classification.METHOD_SIGNATURE ||
                    tok.classification == Token.Classification.LAMBDA_SIGNATURE
            ) {
                callStackReversed.add(new MethodData(tok.startLineNumber, tok.extractedCode));
                if (depth == 0) break;
            }

        }

        return callStackReversed;
    }
}
