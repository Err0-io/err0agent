/*
Copyright 2022 BlueTrailSoftware, Holding Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
                System.out.println("[AGENT-000060] Written: " + path);
            }
            catch (IOException iex) {
                iex.printStackTrace();
                throw new RuntimeException(iex);
            }
        }
    }
}
