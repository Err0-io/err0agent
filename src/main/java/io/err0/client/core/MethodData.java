package io.err0.client.core;

import java.util.regex.Pattern;

public class MethodData {
    private static Pattern reWhitespace = Pattern.compile("\\s+");

    public MethodData(final int line, final String code) {
        this.line = line;
        this.code = reWhitespace.matcher(code.trim()).replaceAll(" ");
    }

    public final int line;
    public final String code;
}
