package io.err0.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonHelper {

    public static String getAsString(JsonObject object, String propertyName, String defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element) {
            return element.getAsString();
        }
        return defaultValue;
    }

    public static int getAsInt(JsonObject object, String propertyName, int defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element) {
            return element.getAsInt();
        }
        return defaultValue;
    }

    public static boolean getAsBoolean(JsonObject object, String propertyName, boolean defaultValue) {
        JsonElement element = object.get(propertyName);
        if (null != element) {
            return element.getAsBoolean();
        }
        return defaultValue;
    }
}
