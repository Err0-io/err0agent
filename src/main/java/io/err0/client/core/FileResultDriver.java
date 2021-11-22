package io.err0.client.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResultDriver implements ResultDriver {
    @Override
    public void processResult(boolean changed, String path, SourceCodeParse parse, Charset fileCharset) {
        if (changed) {
            final StringBuilder output = new StringBuilder();
            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                output.append(parse.tokenList.get(j).source);
            }
            try {
                Files.writeString(Path.of(path), output.toString(), fileCharset);
                System.out.println("Written: " + path);
            }
            catch (IOException iex) {
                iex.printStackTrace();
                throw new RuntimeException(iex);
            }
        }
    }
}
