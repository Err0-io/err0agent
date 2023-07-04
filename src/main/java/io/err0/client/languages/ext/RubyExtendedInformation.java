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

    public HereDoc hereDoc;

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
        } else if (null != hereDoc) {
            return s.trim();
        } else {
            throw new RuntimeException("[AGENT-000098] Not yet implemented.");
        }
    }

    @Override
    public int getStringQuoteWidth() {
        if (percentLiteral) {
            return percentLiteralType == 0 ? 2 : 3;
        } else if (null != hereDoc) {
            int i = 0;
            for (char ch : token.sourceNoErrorCode.toCharArray()) {
                if (Character.isWhitespace(ch)) {
                    ++i;
                } else {
                    break;
                }
            }
            return i;
        } else {
            throw new RuntimeException("[AGENT-000099] Not yet implemented.");
        }
    }

    @Override
    public boolean escapeErrorCode() {
        return percentLiteral && opening == '[';
    }

    public enum HereDocType
    {
        REGULAR,
        INDENTED,
        SQUIGGLY
    }

    public static class HereDoc
    {
        public HereDoc(String label, HereDocType type, char quoteChar)
        {
            this.label = label;
            this.type = type;
            this.quoteChar = quoteChar;
        }

        public boolean interpolated() {
            return quoteChar == 'Q' || quoteChar == 0;
        }

        public String label;
        public HereDocType type;
        public char quoteChar;
    }
}
