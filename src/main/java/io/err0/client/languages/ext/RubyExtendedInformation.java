package io.err0.client.languages.ext;

import io.err0.client.core.Token;
import io.err0.client.core.TokenExtendedInformation;

public class RubyExtendedInformation implements TokenExtendedInformation {

    public RubyExtendedInformation(Token token) {
        this.token = token;
    }

    public boolean percentLiteral = false;
    public char percentLiteralType = 0;
    public char opening = 0;
    public char closing = 0;
    public int curlyBracketIndent = 0;

    private final Token token;

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public String getStringLiteral() {
        String s = null != token.sourceNoErrorCode ? token.sourceNoErrorCode : token.source;
        if (null == s) return null;

        if (percentLiteral) {
            return s.length() > 1 ? s.substring(this.getStringQuoteWidth(), s.length() - 1) : "";
        } else {
            throw new RuntimeException("Not yet implemented.");
        }
    }

    @Override
    public int getStringQuoteWidth() {
        if (percentLiteral) {
            return percentLiteralType == 0 ? 2 : 3;
        } else {
            throw new RuntimeException("Not yet implemented.");
        }
    }

    @Override
    public boolean escapeErrorCode() {
        return percentLiteral && opening == '[';
    }
}
