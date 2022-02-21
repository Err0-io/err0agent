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
        codePolicyJson.addProperty("mode", 0);
        this.mode = CodePolicyMode.DEFAULTS;
    }

    public CodePolicy(final JsonObject codePolicyJson)
    {
        this.codePolicyJson = codePolicyJson;
        int i = this.codePolicyJson.get("mode").getAsInt();
        switch (i) {
            case 0:
                this.mode = CodePolicyMode.DEFAULTS;
                break;
            case 1:
                this.mode = CodePolicyMode.EASY_CONFIGURATION;
                break;
            case 2:
                this.mode = CodePolicyMode.ADVANCED_CONFIGURATION;
                break;
            default:
                throw new RuntimeException("Unknown mode");
        }
    }

    final JsonObject codePolicyJson;
    public final CodePolicyMode mode;

    public String easyModeObjectPattern() {
        if (mode != CodePolicyMode.EASY_CONFIGURATION && mode != CodePolicyMode.ADVANCED_CONFIGURATION)
            throw new RuntimeException("System error, easy and advanced configuration only.");
        JsonElement el = codePolicyJson.get("easy_mode_logger_object_naming_pattern");
        if (null == el) {
            // defaults:
            return "(m?_?)*log(ger)?";
        } else {
            return el.getAsString();
        }
    }

    public String easyModeMethodPattern() {
        if (mode != CodePolicyMode.EASY_CONFIGURATION && mode != CodePolicyMode.ADVANCED_CONFIGURATION)
            throw new RuntimeException("System error, easy and advanced configuration only.");
        JsonElement el = codePolicyJson.get("easy_mode_logger_method_naming_pattern");
        if (null == el) {
            // defaults:
            return "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)";
        } else {
            return el.getAsString();
        }
    }
}
