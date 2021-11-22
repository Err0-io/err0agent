package io.err0.client.core;

import java.nio.charset.Charset;

public interface ResultDriver {
    void processResult(final boolean changed, final String path, final SourceCodeParse parse, final Charset fileCharset);
}
