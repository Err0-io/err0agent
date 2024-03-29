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
        int i = GsonHelper.asInt(this.codePolicyJson, "mode", -1);
        switch (i) {
            case 0:
                this.mode = CodePolicyMode.DEFAULTS;
                break;
            case 1:
                this.mode = CodePolicyMode.EASY_CONFIGURATION;
                this.disableLogs = GsonHelper.asBoolean(this.codePolicyJson, "disable_logs", false);
                this.disableExceptions = GsonHelper.asBoolean(this.codePolicyJson, "disable_exceptions", false);
                this.enablePlaceholder = GsonHelper.asBoolean(this.codePolicyJson, "enable_placeholder", false);
                this.placeholderValue = GsonHelper.asString(this.codePolicyJson, "placeholder_value", null);
                break;
            case 2:
                this.mode = CodePolicyMode.ADVANCED_CONFIGURATION;
                this.disableLogs = GsonHelper.asBoolean(this.codePolicyJson, "disable_logs", false);
                this.disableExceptions = GsonHelper.asBoolean(this.codePolicyJson, "disable_exceptions", false);
                this.enablePlaceholder = GsonHelper.asBoolean(this.codePolicyJson, "enable_placeholder", false);
                this.placeholderValue = GsonHelper.asString(this.codePolicyJson, "placeholder_value", null);
                JsonElement e = codePolicyJson.get("adv_csharp");
                this.adv_csharp = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_golang");
                this.adv_golang = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_java");
                this.adv_java = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_js");
                this.adv_js = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_php");
                this.adv_php = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_python");
                this.adv_python = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_ts");
                this.adv_ts = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_ccpp");
                this.adv_ccpp = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_rust");
                this.adv_rust = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_lua");
                this.adv_lua = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_ruby");
                this.adv_ruby = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_swift");
                this.adv_swift = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_kotlin");
                this.adv_kotlin = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                e = codePolicyJson.get("adv_objc");
                this.adv_objc = null == e ? new LanguageCodePolicy(new JsonObject(), this) : new LanguageCodePolicy(e.getAsJsonObject(), this);
                break;
            default:
                throw new RuntimeException("[AGENT-000009] Unknown mode");
        }

        if (this.enablePlaceholder && (null == this.placeholderValue || "".equals(this.placeholderValue))) {
            throw new RuntimeException("[AGENT-000102] Invalid placeholder settings.");
        }
    }

    final JsonObject codePolicyJson;
    public final CodePolicyMode mode;

    boolean disableLogs = false;
    boolean disableExceptions = false;
    boolean enablePlaceholder = false;
    String placeholderValue = null;

    public boolean getDisableLogs() { return this.disableLogs; }
    public boolean getDisableExceptions() { return this.disableExceptions; }
    public boolean getEnablePlaceholder() { return this.enablePlaceholder; }
    public String getPlaceholderValue() { return this.placeholderValue; }

    public LanguageCodePolicy adv_csharp;
    public LanguageCodePolicy adv_golang;
    public LanguageCodePolicy adv_java;
    public LanguageCodePolicy adv_js;
    public LanguageCodePolicy adv_php;
    public LanguageCodePolicy adv_python;
    public LanguageCodePolicy adv_ts;
    public LanguageCodePolicy adv_ccpp;
    public LanguageCodePolicy adv_rust;
    public LanguageCodePolicy adv_lua;
    public LanguageCodePolicy adv_ruby;
    public LanguageCodePolicy adv_swift;
    public LanguageCodePolicy adv_kotlin;
    public LanguageCodePolicy adv_objc;

    public String easyModeObjectPattern() {
        JsonElement el = codePolicyJson.get("easy_mode_logger_object_naming_pattern");
        if (null == el) {
            // defaults:
            return "(m?_?)*log(ger)?";
        } else {
            return el.getAsString();
        }
    }

    public String easyModeMethodPattern() {
        JsonElement el = codePolicyJson.get("easy_mode_logger_method_naming_pattern");
        if (null == el) {
            // defaults:
            return "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info|fault|notice)";
        } else {
            return el.getAsString();
        }
    }
}
