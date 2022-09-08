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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class GsonHelper {

    public static String asString(JsonObject object, String propertyName, String defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element && !element.getClass().equals(JsonNull.class)) {
            return element.getAsString();
        }
        return defaultValue;
    }

    public static int asInt(JsonObject object, String propertyName, int defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element && !element.getClass().equals(JsonNull.class)) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    public static boolean asBoolean(JsonObject object, String propertyName, boolean defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element && !element.getClass().equals(JsonNull.class)) {
            return element.getAsBoolean();
        }
        return defaultValue;
    }
}
