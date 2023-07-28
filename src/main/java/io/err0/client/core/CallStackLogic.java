package io.err0.client.core;

import java.util.ArrayList;

public interface CallStackLogic {
    ArrayList<MethodData> reversed(final int j, final SourceCodeParse parse, final Token currentToken);
}
