package io.err0.client.core;

public class TokenStateItem {
    TokenStateItem(final StateItem stateItem, final Token token, final long inputErrorOrdinal) {
        this.stateItem = stateItem;
        this.token = token;
        this.inputErrorOrdinal = inputErrorOrdinal;
    }

    public final StateItem stateItem;
    public final Token token;
    public final long inputErrorOrdinal;
    public int nMatchFromMethod = 0;
    public int nMatchFromFile = 0;
}
