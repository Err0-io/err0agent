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

import java.util.UUID;

public class RealmPolicy {

    /**
     * Load a realm policy from the web service, note different format.
     * FIXME - formats.
     * @param realmJson
     */
    public RealmPolicy(final JsonObject realmJson) {
        this.realmJson = realmJson;
        JsonObject realmData = realmJson.get("data").getAsJsonObject();

        this.name = GsonHelper.asString(realmData, "name", null);
        this.realm_code = GsonHelper.asString(realmData, "realm_code", null);
        this.realm_uuid = UUID.fromString(GsonHelper.asString(realmJson, "pk", null));
        this.customer_uuid = null;
        this.policy_editable_by_prj = GsonHelper.asBoolean(realmData, "policy_editable_by_prj", false);
        this.context_allowed_in_prj = GsonHelper.asBoolean(realmData, "context_allowed_in_prj", true);
        final JsonObject policyJson = realmData.getAsJsonObject("policy");
        this.error_prefix = GsonHelper.asString(policyJson, "error_prefix", null);
        //this.error_template = GsonHelper.getAsString(policyJson, "error_template", null);
        this.error_pad_to_n = GsonHelper.asInt(policyJson, "error_pad_to_n", -1);
        this.context = GsonHelper.asBoolean(policyJson, "context", false);
        this.context_n_lines = GsonHelper.asInt(policyJson, "context_n_lines", 0);

        JsonElement realm_code_policy = realmData.get("realm_code_policy");
        if (null != realm_code_policy) {
            this.realm_code_policy = new CodePolicy(realm_code_policy.getAsJsonObject());
        } else {
            this.realm_code_policy = new CodePolicy(); // defaults
        }
    }

    public final JsonObject realmJson;
    final String name;
    final String realm_code;
    final UUID realm_uuid;
    public UUID customer_uuid = null;
    final boolean policy_editable_by_prj;
    final boolean context_allowed_in_prj;
    final String error_prefix;
    //final String error_template;
    final int error_pad_to_n;
    final boolean context;
    final int context_n_lines;

    final CodePolicy realm_code_policy;

    String error_sequence_generator = null;
    public String getErrorSequenceName() {
        // TODO: do error numbers belong to the realm or to the app, here they belong to the app.
        if (null == error_sequence_generator) {
            error_sequence_generator = "s_rlm_errno_" + realm_uuid.toString().replaceAll("-", "_");
        }
        return error_sequence_generator;
    }
}
