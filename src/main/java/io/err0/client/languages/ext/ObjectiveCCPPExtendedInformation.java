package io.err0.client.languages.ext;

import io.err0.client.core.Token;
import io.err0.client.core.TokenExtendedInformation;

public class ObjectiveCCPPExtendedInformation implements TokenExtendedInformation {

    public ObjectiveCCPPExtendedInformation(final Token token, final int stringQuoteWidth) {
        this.token = token;
        this.stringQuoteWidth = stringQuoteWidth;
    }

    private final Token token;
    private final int stringQuoteWidth;

    @Override
    public String getStringLiteral() {
        String s = null != token.sourceNoErrorCode ? token.sourceNoErrorCode : token.source;
        if (null == s) return null;
        return s.length() > 1 ? s.substring(this.getStringQuoteWidth(), s.length() - 1) : "";
    }

    @Override
    public int getStringQuoteWidth() {
        return this.stringQuoteWidth;
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
