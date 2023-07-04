package io.err0.client.core;

public interface TokenExtendedInformation {
    String getStringLiteral();
    int getStringQuoteWidth();
    Token getToken();
    boolean escapeErrorCode();
}
