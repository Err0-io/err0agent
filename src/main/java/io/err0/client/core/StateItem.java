package io.err0.client.core;

import java.nio.charset.Charset;

public class StateItem {
    StateItem(final String localToCheckoutUnchanged, final String localToCheckoutLower, final SourceCodeParse parse, final Charset fileCharset) {
        this.localToCheckoutUnchanged = localToCheckoutUnchanged;
        this.localToCheckoutLower = localToCheckoutLower;
        this.parse = parse;
        this.fileCharset = fileCharset;
    }

    public final String localToCheckoutUnchanged;
    public final String localToCheckoutLower;
    public final SourceCodeParse parse;
    public final Charset fileCharset;

    public boolean getChanged() {
        for (int i = 0, l = this.parse.tokenList.size(); i<l; ++i) {
            Token t = this.parse.tokenList.get(i);
            if (! (t.initialSource.hashCode() == t.source.hashCode() && t.initialSource.equals(t.source))) return true;
        }
        return false;
    }
}
