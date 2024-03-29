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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.err0.client.rules.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class ProjectPolicy {
    /**
     * Load an app policy from the web service, note different format.
     * FIXME - formats.
     */
    public ProjectPolicy(final RealmPolicy realmPolicy, final JsonObject applicationJson) {
        this.realmPolicy = realmPolicy;

        this.applicationJson = applicationJson;
        JsonObject applicationData = applicationJson.get("data").getAsJsonObject();

        this.name = GsonHelper.asString(applicationData, "name", null);
        this.prj_code = GsonHelper.asString(applicationData, "prj_code", null);
        this.prj_uuid = UUID.fromString(GsonHelper.asString(applicationJson, "pk", null));
        final JsonObject policyJson = GsonHelper.asJsonObject(applicationData, "policy", new JsonObject());
        this.error_prefix = GsonHelper.asString(policyJson, "error_prefix", null);
        //this.error_template = GsonHelper.getAsString(policyJson, "error_template", null);
        this.has_error_pad_to_n = policyJson.has("error_pad_to_n") && !policyJson.get("error_pad_to_n").isJsonNull();
        this.error_pad_to_n = GsonHelper.asInt(policyJson, "error_pad_to_n", -1);
        this.has_context = policyJson.has("context") && !policyJson.get("context").isJsonNull();
        this.context = GsonHelper.asBoolean(policyJson, "context", false);
        this.has_context_n_lines = policyJson.has("context_n_lines") && !policyJson.get("context_n_lines").isJsonNull();
        this.context_n_lines = GsonHelper.asInt(policyJson, "context_n_lines", 0);
        this.renumber_on_next_run = GsonHelper.asBoolean(applicationData, "renumber_on_next_run", false);
        final JsonObject sourcesJson = GsonHelper.asJsonObject(applicationData, "sources", new JsonObject());
        final JsonArray includeDirsAry = sourcesJson.getAsJsonArray("include_dirs");
        if (null == includeDirsAry || includeDirsAry.isEmpty()) {
            includeDirs.add(".");
        } else {
            for (int i = 0, l = includeDirsAry.size(); i < l; ++i) {
                includeDirs.add(includeDirsAry.get(i).getAsString());
            }
        }
        final JsonArray excludeDirsAry = sourcesJson.getAsJsonArray("exclude_dirs");
        if (null != excludeDirsAry) {
            for (int i = 0, l = excludeDirsAry.size(); i < l; ++i) {
                excludeDirs.add(excludeDirsAry.get(i).getAsString());
            }
        }
        final JsonArray excludeFilePatternsAry = sourcesJson.getAsJsonArray("exclude_file_patterns");
        if (null != excludeFilePatternsAry) {
            for (int i = 0, l = excludeFilePatternsAry.size(); i < l; ++i) {
                excludeFilePatterns.add(Pattern.compile(excludeFilePatternsAry.get(i).getAsString(), Pattern.CASE_INSENSITIVE)); // Case insensitive for windows compatiblity
            }
        }

        parseExceptionRules(applicationData.getAsJsonArray("exception_rules"));

        JsonElement prj_code_policy = applicationData.get("prj_code_policy");
        if (null == prj_code_policy || prj_code_policy.isJsonNull()) {
            this.prj_code_policy = null;
        } else {
            this.prj_code_policy = new CodePolicy(prj_code_policy.getAsJsonObject());
        }
    }

    String getStringOrNull(JsonObject o, String p) {
        JsonElement element = o.get(p);
        if (element.isJsonNull()) { return null; }
        return element.getAsString();
    }

    void parseExceptionRules(JsonArray rules) {
        if (null == rules) return;
        for (int i=0, l=rules.size(); i<l; ++i) {
            JsonObject ruleJson = rules.get(i).getAsJsonObject();
            ExceptionRule rule = new ExceptionRule();
            rule.name = GsonHelper.asString(ruleJson, "name", "");
            rule.selectorAnd = GsonHelper.asBoolean(ruleJson, "selector_and", false);
            JsonArray selectorAry = ruleJson.get("selectors").getAsJsonArray();
            for (int j=0, m=selectorAry.size(); j<m; ++j) {
                JsonObject selectorJson = selectorAry.get(j).getAsJsonObject();
                ExceptionRuleSelection selection = new ExceptionRuleSelection(
                        ExceptionRuleSelectorEnum.forValue(GsonHelper.asInt(selectorJson, "selector", -1)),
                        getStringOrNull(selectorJson, "selector_value"),
                        ExceptionRuleCheckEnum.forValue(GsonHelper.asInt(selectorJson, "check", -1)),
                        getStringOrNull(selectorJson, "check_value")
                );
                if (selection.selector != ExceptionRuleSelectorEnum.NO_SELECTOR) {
                    rule.selectors.add(selection);
                }
            }
            JsonArray operationAry = ruleJson.get("operations").getAsJsonArray();
            for (int j=0, m=operationAry.size(); j<m; ++j) {
                JsonObject operationJson = operationAry.get(j).getAsJsonObject();
                ExceptionRuleOperation operation = new ExceptionRuleOperation(
                        ExceptionRuleOperationEnum.forValue(GsonHelper.asInt(operationJson, "operation", -1)),
                        getStringOrNull(operationJson, "operation_value")
                );
                if (operation.operation != ExceptionRuleOperationEnum.NO_OPERATION) {
                    rule.operations.add(operation);
                }
            }
            JsonArray actionAry = ruleJson.get("actions").getAsJsonArray();
            for (int j=0, m=actionAry.size(); j<m; ++j) {
                JsonObject actionJson = actionAry.get(j).getAsJsonObject();
                ExceptionRuleAction action = new ExceptionRuleAction(
                        ExceptionRuleActionEnum.forValue(GsonHelper.asInt(actionJson, "action", -1)),
                        getStringOrNull(actionJson, "action_value")
                );
                if (action.action != ExceptionRuleActionEnum.NO_ACTION) {
                    rule.actions.add(action);
                }
            }

            final int nSelectors = rule.selectors.size();
            final int nOutcomes = rule.operations.size() + rule.actions.size();

            if (nSelectors > 0 && nOutcomes > 0) {
                exceptionRules.add(rule);
            }
        }
    }

    final RealmPolicy realmPolicy;
    public final JsonObject applicationJson;

    public RealmPolicy getRealmPolicy() { return realmPolicy; }

    public final ArrayList<ExceptionRule> exceptionRules = new ArrayList<>();

    final String name;
    final String prj_code;
    final UUID prj_uuid;
    final String error_prefix;
    final boolean has_error_pad_to_n;
    final int error_pad_to_n;
    final boolean has_context;
    final boolean context;
    final boolean has_context_n_lines;
    final int context_n_lines;

    public final boolean renumber_on_next_run;

    final CodePolicy prj_code_policy;

    public final ArrayList<String> includeDirs = new ArrayList<>();
    public final ArrayList<String> excludeDirs = new ArrayList<>();
    public final ArrayList<Pattern> excludeFilePatterns = new ArrayList<>();

    public UUID getRealmUuid() { return realmPolicy.realm_uuid; }
    public UUID getAppUuid() { return prj_uuid; }

    String error_sequence_generator = null;
    public String getErrorSequenceName() {
        // TODO: do error numbers belong to the realm or to the app, here they belong to the app.
        if (null == error_sequence_generator) {
            error_sequence_generator = "s_app_errno_" + prj_uuid.toString().replaceAll("-", "_");
        }
        return error_sequence_generator;
    }

    public String getErrorPrefix() {
        if (realmPolicy.policy_editable_by_prj && (null != error_prefix && !"".equals(error_prefix))) {
            return error_prefix;
        } else {
            if (null != realmPolicy.error_prefix && !"".equals(realmPolicy.error_prefix)) {
                return realmPolicy.error_prefix;
            }
        }
        throw new RuntimeException("[AGENT-000013] Error prefix not set in either application policy or realm policy.");
    }

    private ErrorCodeFormatter errorCodeFormatter = null;
    public ErrorCodeFormatter getErrorCodeFormatter() {
        if (null == errorCodeFormatter) {
            errorCodeFormatter = new ErrorCodeFormatter(this);
        }
        return errorCodeFormatter;
    }

    public int getErrorPadToN() {
        if (realmPolicy.policy_editable_by_prj && has_error_pad_to_n && error_pad_to_n >= 0) {
            return error_pad_to_n;
        } else if (realmPolicy.error_pad_to_n >= 0) {
            return realmPolicy.error_pad_to_n;
        }
        return 0; // no padding.
    }

    public boolean getContext() {
        if (! realmPolicy.context_allowed_in_prj) return false;
        if (realmPolicy.policy_editable_by_prj && has_context) {
            return context;
        } else {
            return realmPolicy.context;
        }
    }

    public int getContextNLines() {
        if (realmPolicy.policy_editable_by_prj && has_context_n_lines) {
            return context_n_lines;
        } else {
            return realmPolicy.context_n_lines;
        }
    }

    // Javascript pattern for finding our error number.
    private Pattern reErrorNumber_js = null;
    public Pattern getReErrorNumber_js() {
        if (null == reErrorNumber_js) {
            reErrorNumber_js = Pattern.compile("^(`|'|\")\\[[^\\]]*?" + getErrorPrefix() + "-(\\d+)[^\\]]*?\\]\\s*");
        }
        return reErrorNumber_js;
    }

    private Pattern reErrorNumber_js_placeholder = null;
    public Pattern getReErrorNumber_js_placeholder() {
        if (null == reErrorNumber_js_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000104] Unable to search for placeholder");
            }
            reErrorNumber_js_placeholder = Pattern.compile("^(`|'|\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_js_placeholder;
    }
    public final int reErrorNumber_js_placeholder_open_close_group = 1;
    public final int reErrorNumber_js_placeholder_number_group = 3;
    
    // C# pattern for finding our error number.
    private Pattern reErrorNumber_cs = null;
    public Pattern getReErrorNumber_cs() {
        if (null == reErrorNumber_cs) {
            reErrorNumber_cs = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_cs;
    }

    private Pattern reErrorNumber_cs_placeholder = null;
    public Pattern getReErrorNumber_cs_placeholder() {
        if (null == reErrorNumber_cs_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000105] Unable to search for placeholder");
            }
            reErrorNumber_cs_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_cs_placeholder;
    }
    public final int reErrorNumber_cs_placeholder_open_close_group = 1;
    public final int reErrorNumber_cs_placeholder_number_group = 3;

    // C/C++ pattern for finding our error number.
    private Pattern reErrorNumber_ccpp = null;
    public Pattern getReErrorNumber_ccpp() {
        if (null == reErrorNumber_ccpp) {
            reErrorNumber_ccpp = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_ccpp;
    }

    private Pattern reErrorNumber_ccpp_placeholder = null;
    public Pattern getReErrorNumber_ccpp_placeholder() {
        if (null == reErrorNumber_ccpp_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000106] Unable to search for placeholder");
            }
            reErrorNumber_ccpp_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_ccpp_placeholder;
    }
    public final int reErrorNumber_ccpp_placeholder_open_close_group = 1;
    public final int reErrorNumber_ccpp_placeholder_number_group = 3;

    // Objective C/C++ pattern for finding our error number.
    private Pattern reErrorNumber_objc = null;
    public Pattern getReErrorNumber_objc() {
        if (null == reErrorNumber_objc) {
            reErrorNumber_objc = Pattern.compile("^@?\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_objc;
    }

    private Pattern reErrorNumber_objc_placeholder = null;
    public Pattern getReErrorNumber_objc_placeholder() {
        if (null == reErrorNumber_objc_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000122] Unable to search for placeholder");
            }
            reErrorNumber_objc_placeholder = Pattern.compile("^@?\"(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\"$");
        }
        return reErrorNumber_objc_placeholder;
    }
    public final int reErrorNumber_objc_placeholder_number_group = 1;

    // GO pattern for finding our error number.
    private Pattern reErrorNumber_go = null;
    public Pattern getReErrorNumber_go() {
        if (null == reErrorNumber_go) {
            reErrorNumber_go = Pattern.compile("^(`|'|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_go;
    }

    private Pattern reErrorNumber_go_placeholder = null;
    public Pattern getReErrorNumber_go_placeholder() {
        if (null == reErrorNumber_go_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000107] Unable to search for placeholder");
            }
            reErrorNumber_go_placeholder = Pattern.compile("^(`|'|\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_go_placeholder;
    }
    public final int reErrorNumber_go_placeholder_open_close_group = 1;
    public final int reErrorNumber_go_placeholder_number_group = 3;

    // Rust pattern for finding our error number.
    private Pattern reErrorNumber_rust = null;
    public Pattern getReErrorNumber_rust() {
        if (null == reErrorNumber_rust) {
            reErrorNumber_rust = Pattern.compile("^(\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_rust;
    }

    private Pattern reErrorNumber_rust_placeholder = null;
    public Pattern getReErrorNumber_rust_placeholder() {
        if (null == reErrorNumber_rust_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000108] Unable to search for placeholder");
            }
            reErrorNumber_rust_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_rust_placeholder;
    }
    public final int reErrorNumber_rust_placeholder_open_close_group = 1;
    public final int reErrorNumber_rust_placeholder_number_group = 3;

    // Java pattern for finding our error number.
    private Pattern reErrorNumber_java = null;
    public Pattern getReErrorNumber_java() {
        if (null == reErrorNumber_java) {
            reErrorNumber_java = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_java;
    }

    // Java pattern for finding our error number, multi-line strings.
    private Pattern reErrorNumber_java_textblocks = null;
    public Pattern getReErrorNumber_java_textblocks() {
        if (null == reErrorNumber_java_textblocks) {
            reErrorNumber_java_textblocks = Pattern.compile("^\"\"\"(\\s*)\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_java_textblocks;
    }

    private Pattern reErrorNumber_java_placeholder = null;
    public Pattern getReErrorNumber_java_placeholder() {
        if (null == reErrorNumber_java_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000109] Unable to search for placeholder");
            }
            reErrorNumber_java_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_java_placeholder;
    }
    public final int reErrorNumber_java_placeholder_open_close_group = 1;
    public final int reErrorNumber_java_placeholder_number_group = 3;

    // PHP pattern for finding our error number.
    private Pattern reErrorNumber_php = null;
    public Pattern getReErrorNumber_php() {
        if (null == reErrorNumber_php) {
            reErrorNumber_php = Pattern.compile("^('|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_php;
    }

    private Pattern reErrorNumber_php_placeholder = null;
    public Pattern getReErrorNumber_php_placeholder() {
        if (null == reErrorNumber_php_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000110] Unable to search for placeholder");
            }
            reErrorNumber_php_placeholder = Pattern.compile("^('|\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_php_placeholder;
    }
    public final int reErrorNumber_php_placeholder_open_close_group = 1;
    public final int reErrorNumber_php_placeholder_number_group = 3;

    // TypeScript pattern for finding our error number.
    private Pattern reErrorNumber_ts = null;
    public Pattern getReErrorNumber_ts() {
        if (null == reErrorNumber_ts) {
            reErrorNumber_ts = Pattern.compile("^(`|'|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_ts;
    }

    private Pattern reErrorNumber_ts_placeholder = null;
    public Pattern getReErrorNumber_ts_placeholder() {
        if (null == reErrorNumber_ts_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000111] Unable to search for placeholder");
            }
            reErrorNumber_ts_placeholder = Pattern.compile("^(`|'|\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_ts_placeholder;
    }
    public final int reErrorNumber_ts_placeholder_open_close_group = 1;
    public final int reErrorNumber_ts_placeholder_number_group = 3;

    private Pattern reErrorNumber_py = null;
    public Pattern getReErrorNumber_py() {
        if (null == reErrorNumber_py) {
            reErrorNumber_py = Pattern.compile("^(['\"])\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_py;
    }

    private Pattern reErrorNumber_py_placeholder = null;
    public Pattern getReErrorNumber_py_placeholder() {
        if (null == reErrorNumber_py_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000112] Unable to search for placeholder");
            }
            reErrorNumber_py_placeholder = Pattern.compile("^(['\"]|'''|\"\"\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_py_placeholder;
    }
    public final int reErrorNumber_py_placeholder_open_close_group = 1;
    public final int reErrorNumber_py_placeholder_number_group = 3;

    private Pattern reErrorNumber_py_textblocks = null;
    public Pattern getReErrorNumber_py_textblocks() {
        if (null == reErrorNumber_py_textblocks) {
            reErrorNumber_py_textblocks = Pattern.compile("^('''|\"\"\")(\\s*)\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_py_textblocks;
    }

    private Pattern reErrorNumber_lua = null;
    public Pattern getReErrorNumber_lua() {
        if (null == reErrorNumber_lua) {
            reErrorNumber_lua = Pattern.compile("^('|\"|(\\[=*\\[))\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_lua;
    }

    private Pattern reErrorNumber_lua_placeholder = null;
    public Pattern getReErrorNumber_lua_placeholder() {
        if (null == reErrorNumber_lua_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000113] Unable to search for placeholder");
            }
            reErrorNumber_lua_placeholder = Pattern.compile("^('|\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_lua_placeholder;
    }
    public final int reErrorNumber_lua_placeholder_open_close_group = 1;
    public final int reErrorNumber_lua_placeholder_number_group = 3;

    private Pattern reErrorNumber_rb = null;
    public Pattern getReErrorNumber_rb() {
        if (null == reErrorNumber_rb) {
            reErrorNumber_rb = Pattern.compile("^('|\"|%[Qq]?.)\\\\?\\[" + getErrorPrefix() + "-(\\d+)\\\\?\\]\\s*");
        }
        return reErrorNumber_rb;
    }

    private Pattern reErrorNumber_rb_hereDoc = null;
    public Pattern getReErrorNumber_rb_hereDoc() {
        if (null == reErrorNumber_rb_hereDoc) {
            reErrorNumber_rb_hereDoc = Pattern.compile("^(\\s*)\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_rb_hereDoc;
    }

    private Pattern reErrorNumber_rb_placeholder = null;
    public Pattern getReErrorNumber_rb_placeholder() {
        if (null == reErrorNumber_rb_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000114] Unable to search for placeholder");
            }
            reErrorNumber_rb_placeholder = Pattern.compile("^(['\"])(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_rb_placeholder;
    }
    public final int reErrorNumber_rb_placeholder_open_close_group = 1;
    public final int reErrorNumber_rb_placeholder_number_group = 3;

    // Swift pattern for finding our error number.
    private Pattern reErrorNumber_swift = null;
    public Pattern getReErrorNumber_swift() {
        if (null == reErrorNumber_swift) {
            reErrorNumber_swift = Pattern.compile("^#?\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_swift;
    }

    // Swift pattern for finding our error number, multi-line strings.
    private Pattern reErrorNumber_swift_textblocks = null;
    public Pattern getReErrorNumber_swift_textblocks() {
        if (null == reErrorNumber_swift_textblocks) {
            reErrorNumber_swift_textblocks = Pattern.compile("^#?\"\"\"(\\s*)\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_swift_textblocks;
    }

    private Pattern reErrorNumber_swift_placeholder = null;
    public Pattern getReErrorNumber_swift_placeholder() {
        if (null == reErrorNumber_swift_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000118] Unable to search for placeholder");
            }
            reErrorNumber_swift_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_swift_placeholder;
    }
    public final int reErrorNumber_swift_placeholder_open_close_group = 1;
    public final int reErrorNumber_swift_placeholder_number_group = 3;

    private Pattern reErrorNumber_kotlin = null;
    public Pattern getReErrorNumber_kotlin() {
        if (null == reErrorNumber_kotlin) {
            reErrorNumber_kotlin = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_kotlin;
    }

    // Java pattern for finding our error number, multi-line strings.
    private Pattern reErrorNumber_kotlin_textblocks = null;
    public Pattern getReErrorNumber_kotlin_textblocks() {
        if (null == reErrorNumber_kotlin_textblocks) {
            reErrorNumber_kotlin_textblocks = Pattern.compile("^\"\"\"(\\s*)\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_kotlin_textblocks;
    }

    private Pattern reErrorNumber_kotlin_placeholder = null;
    public Pattern getReErrorNumber_kotlin_placeholder() {
        if (null == reErrorNumber_kotlin_placeholder) {
            CodePolicy codePolicy = getCodePolicy();
            if (!codePolicy.enablePlaceholder) {
                throw new RuntimeException("[AGENT-000120] Unable to search for placeholder");
            }
            reErrorNumber_kotlin_placeholder = Pattern.compile("^(\")(" + codePolicy.placeholderValue + "|" + getErrorPrefix() + "-(\\d+))\\1$");
        }
        return reErrorNumber_kotlin_placeholder;
    }
    public final int reErrorNumber_kotlin_placeholder_open_close_group = 1;
    public final int reErrorNumber_kotlin_placeholder_number_group = 3;

    public CodePolicy getCodePolicy() {
        if (null == this.prj_code_policy) {
            return realmPolicy.realm_code_policy;
        } else {
            return this.prj_code_policy;
        }
    }
}
