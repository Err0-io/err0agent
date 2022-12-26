package io.err0.client.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static final Path pathOf(final String path) {
        return Paths.get(path);
    }

    public static String readString(final Path path) throws IOException {
        return readString(path, StandardCharsets.UTF_8);
    }

    public static String readString(final Path path, final Charset charset) throws IOException {
        byte bytes[] = Files.readAllBytes(path);
        return new String(bytes, charset);
    }

    public static void writeString(final Path path, final String content) throws IOException {
        writeString(path, content, StandardCharsets.UTF_8);
    }

    public static void writeString(final Path path, final String content, final Charset charset) throws IOException {
        byte bytes[] = content.getBytes(charset);
        Files.write(path, bytes);
    }

    public static String stripLeading(final String str) {
        char data[] = str.toCharArray();
        int i = 0, l = data.length;
        for (; i < l && Character.isWhitespace(data[i]) ; ++i) {}
        return str.substring(i);
    }

    public static byte[] readAllBytes(final InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

}
