package io.err0.client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class RestApiProvider implements ApiProvider {

    public RestApiProvider(final String tokenPath) throws IOException {
        httpClient = HttpClients.createDefault();

        tokenJson = JsonParser.parseString(Files.readString(Path.of(tokenPath))).getAsJsonObject();
    }

    private CloseableHttpClient httpClient = null;
    private final JsonObject tokenJson;

    public void getPolicy(Consumer<JsonObject> onResult) {
        cache.clear();
        validErrorNumbers.clear();

        try {
            HttpGet request = new HttpGet("https://" + tokenJson.get("host").getAsString() + "/~/client/get-policy");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());

            httpClient.execute(request, response -> {
                onResult.accept(JsonParser.parseString(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
                return null;
            });

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ensurePolicyIsSetUp(ProjectPolicy policy) {
        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/ensure-policy");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            StringEntity stringEntity = new StringEntity(new JsonObject().toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    // ignore :-)
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
                return null;
            });

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (null != httpClient) {
            try { httpClient.close(); } catch (IOException ex) { /* ignore */ }
            httpClient = null;
        }
    }

    HashSet<Long> validErrorNumbers = new HashSet<>();

    @Override
    public void cacheAllValidErrorNumbers(ProjectPolicy policy) {
        validErrorNumbers.clear();
        try {
            HttpGet request = new HttpGet("https://" + tokenJson.get("host").getAsString() + "/~/client/get-valid-error-numbers");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());

            httpClient.execute(request, response -> {
                JsonObject o = JsonParser.parseString(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                if (o.get("success").getAsBoolean()) {
                    JsonArray a = o.get("valid").getAsJsonArray();
                    for (int i = 0, l = a.size(); i < l; ++i)
                        validErrorNumbers.add(a.get(i).getAsLong());
                    return true;
                }
                return false;
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validErrorNumber(ProjectPolicy policy, long errorOrdinal) {
        return validErrorNumbers.contains(errorOrdinal);
    }

    private LinkedList<Long> cache = new LinkedList<>();

    public long nextErrorNumber(ProjectPolicy policy) {
        long l = cache.getFirst();
        cache.removeFirst();
        return l;
    }

    @Override
    public void cacheErrorNumberBatch(ProjectPolicy policy, long number) {
        cache.clear();

        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/get-next-error-number-batch");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("n", number);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            cache = httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    JsonArray error_ordinals = json.get("error_ordinals").getAsJsonArray();
                    LinkedList<Long> eo = new LinkedList<>();
                    for (int i = 0, l = error_ordinals.size(); i < l; ++i) {
                        eo.add(error_ordinals.get(i).getAsLong());
                    }
                    return eo;
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bulkInsertMetaData(ProjectPolicy policy, UUID run_uuid, String errorPrefix, ArrayList<ForInsert> forBulkInsert) {
        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/bulk-insert-meta-data");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("run_uuid", run_uuid.toString());
            post.addProperty("error_prefix", errorPrefix);
            JsonArray bulk = new JsonArray();
            forBulkInsert.forEach(forInsert -> {
                JsonObject insert = new JsonObject();
                insert.addProperty("error_code", forInsert.errorCode);
                insert.addProperty("error_ordinal", forInsert.errorOrdinal);
                insert.add("metadata", forInsert.metaData);
                bulk.add(insert);
            });
            post.add("bulk", bulk);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    return true;
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setNextErrorNumber(ProjectPolicy policy, long nextErrorNumber) {
        throw new RuntimeException("[AGENT-000014] Not implemented.");
    }

    @Override
    public UUID createRun(ProjectPolicy policy, JsonObject appGitMetadata, JsonObject runGitMetadata, String runState) {

        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/create-run");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.add("app_git_metadata", appGitMetadata);
            post.add("run_git_metadata", runGitMetadata);
            post.addProperty("run_state", runState);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            return httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    return UUID.fromString(json.get("run_uuid").getAsString());
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateRun(ProjectPolicy policy, UUID run_uuid, JsonObject gitMetadata, JsonObject runMetadata) {
        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/update-run");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("run_uuid", run_uuid.toString());
            post.add("git_metadata", gitMetadata);
            post.add("run_metadata", runMetadata);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    return true;
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void importPreviousState(ProjectPolicy policy, GlobalState globalState) {
        globalState.previousRunSignatures.clear();

        try {
            HttpGet request = new HttpGet("https://" + tokenJson.get("host").getAsString() + "/~/client/get-previous-state");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());

            httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    JsonArray error_codes = json.get("error_codes").getAsJsonArray();
                    for (int i = 0, l = error_codes.size(); i < l; ++i) {
                        JsonObject x = error_codes.get(i).getAsJsonObject();
                        long error_ordinal = x.get("error_ordinal").getAsLong();
                        JsonObject metadata = x.get("metadata").getAsJsonObject();
                        globalState.previousRunSignatures.put(error_ordinal, new Signature(metadata));
                    }
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
                return null;
            });

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finaliseRun(ProjectPolicy policy, UUID run_uuid) {
        try {
            HttpPost request = new HttpPost("https://" + tokenJson.get("host").getAsString() + "/~/client/finalise-run");
            request.addHeader("Authorization", "token " + tokenJson.get("token_value").getAsString());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("run_uuid", run_uuid.toString());

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                String jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonAsString).getAsJsonObject();
                if (json.get("success").getAsBoolean()) {
                    return null;
                } else {
                    // FIXME - proper error/fault reports...
                    throw new RuntimeException(jsonAsString);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
