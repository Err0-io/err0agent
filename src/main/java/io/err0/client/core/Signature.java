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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Signature {
    public Signature(JsonObject metaData) {
        this.lastLineNumber = metaData.get("line").getAsLong();
        int n = 1;
        JsonArray ary = metaData.getAsJsonArray("methods");
        if (null != ary) {
            n += ary.size();
        }
        methodSignatureComponents = new String[n];
        filenameLower = methodSignatureComponents[0] = metaData.get("filename").getAsString(); // relative to checkout directory
        // TODO: if we know package name, put it here...
        for (int i = 1; i < n; ++i) {
            methodSignatureComponents[i] = ary.get(i - 1).getAsJsonObject().get("c").getAsString();
        }
    }

    public final long lastLineNumber;
    public final String[] methodSignatureComponents;
    public final String filenameLower;
}
