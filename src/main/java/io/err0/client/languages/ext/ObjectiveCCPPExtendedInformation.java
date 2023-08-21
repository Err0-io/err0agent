package io.err0.client.languages.ext;

import io.err0.client.core.Token;
import io.err0.client.core.TokenExtendedInformation;

public class ObjectiveCCPPExtendedInformation implements TokenExtendedInformation {

    public ObjectiveCCPPExtendedInformation(final Token token) {
        this.token = token;
    }

    private final Token token;

    @Override
    public String getStringLiteral() {
        String s = null != token.sourceNoErrorCode ? token.sourceNoErrorCode : token.source;
        if (null == s) return null;
        return s.length() > 1 ? s.substring(this.getStringQuoteWidth(), s.length() - 1) : "";
    }

    @Override
    public int getStringQuoteWidth() {
        return 2;
    }

    @Override
    public Token getToken() {
        return this.token;
    }

    @Override
    public boolean escapeErrorCode() {
        return false;
    }
}
