package io.err0.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        this.name = GsonHelper.getAsString(realmData, "name", null);
        this.realm_code = GsonHelper.getAsString(realmData, "realm_code", null);
        this.realm_uuid = UUID.fromString(GsonHelper.getAsString(realmJson, "pk", null));
        this.customer_uuid = null;
        this.policy_editable_by_prj = GsonHelper.getAsBoolean(realmData, "policy_editable_by_prj", false);
        this.context_allowed_in_prj = GsonHelper.getAsBoolean(realmData, "context_allowed_in_prj", true);
        final JsonObject policyJson = realmData.getAsJsonObject("policy");
        this.error_prefix = GsonHelper.getAsString(policyJson, "error_prefix", null);
        this.error_template = GsonHelper.getAsString(policyJson, "error_template", null);
        this.error_pad_to_n = GsonHelper.getAsInt(policyJson, "error_pad_to_n", -1);
        this.context = GsonHelper.getAsBoolean(policyJson, "context", false);
        this.context_n_lines = GsonHelper.getAsInt(policyJson, "context_n_lines", 0);

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
    final String error_template;
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
