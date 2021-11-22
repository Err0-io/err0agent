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
