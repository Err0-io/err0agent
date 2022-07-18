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

        this.name = GsonHelper.getAsString(applicationData, "name", null);
        this.prj_code = GsonHelper.getAsString(applicationData, "prj_code", null);
        this.prj_uuid = UUID.fromString(GsonHelper.getAsString(applicationJson, "pk", null));
        final JsonObject policyJson = applicationData.getAsJsonObject("policy");
        this.error_prefix = GsonHelper.getAsString(policyJson, "error_prefix", null);
        //this.error_template = GsonHelper.getAsString(policyJson, "error_template", null);
        this.error_pad_to_n = GsonHelper.getAsInt(policyJson, "error_pad_to_n", -1);
        this.has_context = policyJson.has("context");
        this.context = GsonHelper.getAsBoolean(policyJson, "context", false);
        this.has_context_n_lines = policyJson.has("context_n_lines");
        this.context_n_lines = GsonHelper.getAsInt(policyJson, "context_n_lines", 0);
        final JsonObject sourcesJson = applicationData.getAsJsonObject("sources");
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
            rule.name = ruleJson.get("name").getAsString();
            rule.selectorAnd = ruleJson.get("selectorAnd").getAsBoolean();
            JsonArray selectorAry = ruleJson.get("selectors").getAsJsonArray();
            for (int j=0, m=selectorAry.size(); j<m; ++j) {
                JsonObject selectorJson = selectorAry.get(j).getAsJsonObject();
                ExceptionRuleSelection selection = new ExceptionRuleSelection(
                        ExceptionRuleSelectorEnum.forValue(selectorJson.get("selector").getAsInt()),
                        getStringOrNull(selectorJson, "selectorValue"),
                        ExceptionRuleCheckEnum.forValue(selectorJson.get("check").getAsInt()),
                        getStringOrNull(selectorJson, "checkValue")
                );
                if (selection.selector != ExceptionRuleSelectorEnum.NO_SELECTOR) {
                    rule.selectors.add(selection);
                }
            }
            JsonArray operationAry = ruleJson.get("operations").getAsJsonArray();
            for (int j=0, m=operationAry.size(); j<m; ++j) {
                JsonObject operationJson = operationAry.get(j).getAsJsonObject();
                ExceptionRuleOperation operation = new ExceptionRuleOperation(
                        ExceptionRuleOperationEnum.forValue(operationJson.get("operation").getAsInt()),
                        getStringOrNull(operationJson, "operationValue")
                );
                if (operation.operation != ExceptionRuleOperationEnum.NO_OPERATION) {
                    rule.operations.add(operation);
                }
            }
            JsonArray actionAry = ruleJson.get("actions").getAsJsonArray();
            for (int j=0, m=actionAry.size(); j<m; ++j) {
                JsonObject actionJson = actionAry.get(j).getAsJsonObject();
                ExceptionRuleAction action = new ExceptionRuleAction(
                        ExceptionRuleActionEnum.forValue(actionJson.get("action").getAsInt()),
                        getStringOrNull(actionJson, "actionValue")
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
    //final String error_template;
    final int error_pad_to_n;
    final boolean has_context;
    final boolean context;
    final boolean has_context_n_lines;
    final int context_n_lines;

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
        if (realmPolicy.policy_editable_by_prj && error_pad_to_n >= 0) {
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

    // C# pattern for finding our error number.
    private Pattern reErrorNumber_cs = null;
    public Pattern getReErrorNumber_cs() {
        if (null == reErrorNumber_cs) {
            reErrorNumber_cs = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_cs;
    }

    // GO pattern for finding our error number.
    private Pattern reErrorNumber_go = null;
    public Pattern getReErrorNumber_go() {
        if (null == reErrorNumber_go) {
            reErrorNumber_go = Pattern.compile("^(`|'|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_go;
    }

    // Java pattern for finding our error number.
    private Pattern reErrorNumber_java = null;
    public Pattern getReErrorNumber_java() {
        if (null == reErrorNumber_java) {
            reErrorNumber_java = Pattern.compile("^\"\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_java;
    }

    // PHP pattern for finding our error number.
    private Pattern reErrorNumber_php = null;
    public Pattern getReErrorNumber_php() {
        if (null == reErrorNumber_php) {
            reErrorNumber_php = Pattern.compile("^(\'|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_php;
    }

    // TypeScript pattern for finding our error number.
    private Pattern reErrorNumber_ts = null;
    public Pattern getReErrorNumber_ts() {
        if (null == reErrorNumber_ts) {
            reErrorNumber_ts = Pattern.compile("^(`|'|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_ts;
    }

    private Pattern reErrorNumber_py = null;
    public Pattern getReErrorNumber_py() {
        if (null == reErrorNumber_py) {
            reErrorNumber_py = Pattern.compile("^('|\"\"\"|\")\\[" + getErrorPrefix() + "-(\\d+)\\]\\s*");
        }
        return reErrorNumber_py;
    }

    public CodePolicy getCodePolicy() {
        if (null == this.prj_code_policy) {
            return realmPolicy.realm_code_policy;
        } else {
            return this.prj_code_policy;
        }
    }
}
