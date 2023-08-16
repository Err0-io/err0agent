package io.err0.client.languages.ext;

import io.err0.client.core.Token;
import io.err0.client.core.TokenExtendedInformation;
import io.err0.client.core.TokenType;

public class SwiftExtendedInformation implements TokenExtendedInformation {

    public SwiftExtendedInformation(Token token) {
        this.token = token;
    }

    final Token token;

    public boolean nonInterpolating = false;

    @Override
    public String getStringLiteral() {
        String s = null != token.sourceNoErrorCode ? token.sourceNoErrorCode : token.source;
        if (null == s) return null;
        return s.length() > 1 ? s.substring(this.getStringQuoteWidth(), s.length() - 1) : "";
    }

    @Override
    public int getStringQuoteWidth() {
        return (nonInterpolating ? 1 : 0) + (token.type == TokenType.QUOT3_LITERAL ? 3 : 1);
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public boolean escapeErrorCode() {
        return false;
    }
}
