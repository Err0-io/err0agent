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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RestApiProvider implements ApiProvider {
    public RestApiProvider(final String tokenPath) throws IOException {
        httpClient = HttpClients.createDefault();

        try {
            tokenJson = JsonParser.parseString(Files.readString(Path.of(tokenPath))).getAsJsonObject();
        }
        catch (RuntimeException exception) {
            throw new ParserException("[AGENT-000028] Invalid token.json", "file: " + tokenPath, exception);
        }
    }

    protected static class ParserException extends RuntimeException {
        protected ParserException(final String message, final String source) {
            super(message + " " + source);
        }
        protected ParserException(final String message, final String source, Throwable throwable) {
            super(message + " " + source, throwable);
        }
    }

    public static class JsonFormattedExceptionHelper {
        public static void formatToStderrAndFail(JsonObject json) {
            if (GsonHelper.asBoolean(json, "success", false)) {
                System.err.println("[AGENT-000064] Logic error, this response is successful.");
            } else if (GsonHelper.asBoolean(json, "fault", false)) {
                System.err.println("[AGENT-000065] Server indicates unhandled exception: " + GsonHelper.asString(json, "fault_message", ""));
            } else {
                System.err.println("[AGENT-000066] Server error code " + GsonHelper.asInt(json, "error_code", 0));
            }
            System.exit(-1);
        }
    }

    protected static class Parser {
        protected static JsonObject parse(final HttpGet request, final ClassicHttpResponse response) {
            if (response.getCode() != 200) {
                throw new ParserException("[AGENT-000029] HTTP status code " + response.getCode() + " " + response.getReasonPhrase(), request.getRequestUri());
            }
            String jsonAsString = null;
            try {
                jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            }
            catch (Throwable throwable) {
                throw new ParserException("[AGENT-000030] Error reading response or invalid UTF-8 encoding", request.getRequestUri(), throwable);
            }
            try {
                return JsonParser.parseString(jsonAsString).getAsJsonObject();
            }
            catch (Throwable throwable) {
                System.err.println("[AGENT-000067] Invalid JSON:\n" + jsonAsString);
                throw new ParserException("[AGENT-000031] Invalid response from API", request.getRequestUri(), throwable);
            }
        }

        protected static JsonObject parse(final HttpPost request, final ClassicHttpResponse response) {
            if (response.getCode() != 200) {
                throw new ParserException("[AGENT-000032] HTTP status code " + response.getCode() + " " + response.getReasonPhrase(), request.getRequestUri());
            }
            String jsonAsString = null;
            try {
                jsonAsString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            }
            catch (Throwable throwable) {
                throw new ParserException("[AGENT-000033] Error reading response or invalid UTF-8 encoding", request.getRequestUri(), throwable);
            }
            try {
                return JsonParser.parseString(jsonAsString).getAsJsonObject();
            }
            catch (Throwable throwable) {
                System.err.println("[AGENT-000068] Invalid JSON:\n" + jsonAsString);
                throw new ParserException("[AGENT-000034] Invalid response from API", request.getRequestUri(), throwable);
            }
        }
    }

    private CloseableHttpClient httpClient = null;
    private final JsonObject tokenJson;

    public void getPolicy(Consumer<JsonObject> onResult) {
        cache.clear();
        validErrorNumbers.clear();

        try {
            HttpGet request = new HttpGet("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/get-policy");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));

            httpClient.execute(request, response -> {
                onResult.accept(Parser.parse(request, response));
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/ensure-policy");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            StringEntity stringEntity = new StringEntity(new JsonObject().toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    // ignore :-)
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
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
            HttpGet request = new HttpGet("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/get-valid-error-numbers");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    JsonArray a = json.get("valid").getAsJsonArray();
                    for (int i = 0, l = a.size(); i < l; ++i)
                        validErrorNumbers.add(a.get(i).getAsLong());
                    return true;
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
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

    @Override
    public void clearErrorNumberCache(ProjectPolicy policy) {
        validErrorNumbers.clear();
    }

    @Override
    public boolean markRenumberingOK(ProjectPolicy policy) {
        AtomicBoolean result = new AtomicBoolean(false);
        try {
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/mark-renumber-ok");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            StringEntity stringEntity = new StringEntity(new JsonObject().toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false) && json.get("ok_to_renumber").getAsBoolean()) {
                    result.set(true);
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                }
                return null;
            });

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.get();
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/get-next-error-number-batch");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("n", number);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            cache = httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    JsonArray error_ordinals = json.get("error_ordinals").getAsJsonArray();
                    LinkedList<Long> eo = new LinkedList<>();
                    for (int i = 0, l = error_ordinals.size(); i < l; ++i) {
                        eo.add(error_ordinals.get(i).getAsLong());
                    }
                    return eo;
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                    return null;
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/bulk-insert-meta-data");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
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
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    return true;
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                    return false;
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/create-run");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.add("app_git_metadata", appGitMetadata);
            post.add("run_git_metadata", runGitMetadata);
            post.addProperty("run_state", runState);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            return httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    return UUID.fromString(GsonHelper.asString(json, "run_uuid", ""));
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                    return null;
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/update-run");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("run_uuid", run_uuid.toString());
            post.add("git_metadata", gitMetadata);
            post.add("run_metadata", runMetadata);

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    return true;
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                    return false;
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String q(String value) {
        if (null == value) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public void importPreviousState(ProjectPolicy policy, GlobalState globalState, String currentBranch) {
        globalState.previousRunSignatures.clear();

        try {
            HttpGet request = new HttpGet("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/get-previous-state?branch=" + q(currentBranch));
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    JsonArray error_codes = json.get("error_codes").getAsJsonArray();
                    for (int i = 0, l = error_codes.size(); i < l; ++i) {
                        JsonObject x = error_codes.get(i).getAsJsonObject();
                        long error_ordinal = x.get("error_ordinal").getAsLong();
                        JsonObject metadata = x.get("metadata").getAsJsonObject();
                        globalState.previousRunSignatures.put(error_ordinal, new Signature(metadata));
                    }
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
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
            HttpPost request = new HttpPost("https://" + GsonHelper.asString(tokenJson, "host", "localhost") + "/~/client/finalise-run");
            request.addHeader("Authorization", "token " + GsonHelper.asString(tokenJson, "token_value", "-"));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JsonObject post = new JsonObject();
            post.addProperty("run_uuid", run_uuid.toString());

            StringEntity stringEntity = new StringEntity(post.toString());
            request.setEntity(stringEntity);

            httpClient.execute(request, response -> {
                JsonObject json = Parser.parse(request, response);
                if (GsonHelper.asBoolean(json, "success", false)) {
                    return null;
                } else {
                    JsonFormattedExceptionHelper.formatToStderrAndFail(json);
                    return null;
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
