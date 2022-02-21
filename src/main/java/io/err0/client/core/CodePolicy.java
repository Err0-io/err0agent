package io.err0.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CodePolicy {

    public enum CodePolicyMode
    {
        DEFAULTS,
        EASY_CONFIGURATION,
        ADVANCED_CONFIGURATION
    }

    public CodePolicy()
    {
        codePolicyJson = new JsonObject();
        codePolicyJson.addProperty("mode", "DEFAULTS");
        this.mode = CodePolicyMode.DEFAULTS;
    }

    public CodePolicy(final JsonObject codePolicyJson)
    {
        this.codePolicyJson = codePolicyJson;
        this.mode = CodePolicyMode.valueOf(this.codePolicyJson.get("mode").getAsString());
    }

    final JsonObject codePolicyJson;
    final CodePolicyMode mode;

    public String easyModeObjectPattern() {
        if (mode != CodePolicyMode.EASY_CONFIGURATION)
            throw new RuntimeException("System error, easy configuration only.");
        JsonElement el = codePolicyJson.get("easy_mode_logger_object_naming_pattern");
        if (null == el) {
            // defaults:
            return "((m?)_)?log(ger)?";
        } else {
            return el.getAsString();
        }
    }

    public String easyModeMethodPattern() {
        if (mode != CodePolicyMode.EASY_CONFIGURATION)
            throw new RuntimeException("System error, easy configuration only.");
        JsonElement el = codePolicyJson.get("easy_mode_logger_method_naming_pattern");
        if (null == el) {
            // defaults:
            return "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)";
        } else {
            return el.getAsString();
        }
    }
}
