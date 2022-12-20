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
                break;
            case 2:
                this.mode = CodePolicyMode.ADVANCED_CONFIGURATION;
                JsonElement e = codePolicyJson.get("adv_csharp");
                this.adv_csharp = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_golang");
                this.adv_golang = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_java");
                this.adv_java = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_js");
                this.adv_js = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_php");
                this.adv_php = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_python");
                this.adv_python = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_ts");
                this.adv_ts = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                e = codePolicyJson.get("adv_ccpp");
                this.adv_ccpp = null == e ? new LanguageCodePolicy(new JsonObject()) : new LanguageCodePolicy(e.getAsJsonObject());
                break;
            default:
                throw new RuntimeException("[AGENT-000009] Unknown mode");
        }
    }

    final JsonObject codePolicyJson;
    public final CodePolicyMode mode;

    public LanguageCodePolicy adv_csharp;
    public LanguageCodePolicy adv_golang;
    public LanguageCodePolicy adv_java;
    public LanguageCodePolicy adv_js;
    public LanguageCodePolicy adv_php;
    public LanguageCodePolicy adv_python;
    public LanguageCodePolicy adv_ts;
    public LanguageCodePolicy adv_ccpp;

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
            return "(crit(ical)?|log|fatal|err(or)?|warn(ing)?|info)";
        } else {
            return el.getAsString();
        }
    }
}
