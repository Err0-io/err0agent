/*
Copyright 2023 ERR0 LLC

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

import java.util.regex.Pattern;

public class MethodData {
    private static Pattern reWhitespace = Pattern.compile("\\s+");

    public MethodData(final int line, final String code, final Token.Classification classification) {
        this.line = line;
        this.code = reWhitespace.matcher(code.trim()).replaceAll(" ");
        this.classification = classification;
    }

    public final int line;
    public final String code;
    public final Token.Classification classification;

    public String getType() {
        switch (classification) {
            case CLASS_SIGNATURE:
                return "class";
            case METHOD_SIGNATURE:
                return "method";
            case CONTROL_SIGNATURE:
                return "control";
            case LAMBDA_SIGNATURE:
                return "lambda";
        }
        throw new RuntimeException("[AGENT-000116] Incorrect classification.");
    }
}
