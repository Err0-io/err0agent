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
