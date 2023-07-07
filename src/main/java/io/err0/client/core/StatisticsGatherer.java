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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsGatherer {

    public final java.util.Date start = new Date();

    public final HashSet<Long> newErrorOrdinals = new HashSet<>();
    public final HashSet<Long> existingErrorOrdinals = new HashSet<>();

    public final HashSet<Long> newErrorOrdinalsInLog = new HashSet<>();
    public final HashSet<Long> existingErrorOrdinalsInLog = new HashSet<>();

    public final HashSet<Long> newErrorOrdinalsInException = new HashSet<>();
    public final HashSet<Long> existingErrorOrdinalsInException = new HashSet<>();

    public final HashSet<Long> newErrorOrdinalsInPlaceholder = new HashSet<>();
    public final HashSet<Long> existingErrorOrdinalsInPlaceholder = new HashSet<>();

    public final HashSet<Long> abandonedErrorOrdinals = new HashSet<>();

    public final HashMap<String, Long> linesPerFile = new HashMap<>();
    public final HashMap<String, Long> errorsPerFile = new HashMap<>();

    public final ArrayList<ApiProvider.ForInsert> results = new ArrayList<>();
    private final HashMap<String, Long> duplicateCheck = new HashMap<>();
    private final HashMap<String, Long> logByLogLevel = new HashMap<>();
    private long n_log_static = 0;
    private long n_log_dynamic = 0;
    private long n_exception_static = 0;
    private long n_exception_dynamic = 0;
    private long n_log_duplicate  = 0;
    private long n_exception_duplicate = 0;

    public void clearResults() {
        results.clear();
        duplicateCheck.clear();
        logByLogLevel.clear();
        n_log_static = 0;
        n_log_dynamic = 0;
        n_exception_static = 0;
        n_exception_dynamic = 0;
        n_log_duplicate = 0;
        n_exception_duplicate = 0;
    }

    public void storeResult(ApiProvider.ForInsert result) {
        results.add(result);

        final String cleanedMessage = result.metaData.has("cleaned_message") ? result.metaData.get("cleaned_message").getAsString() : null;
        if (null != cleanedMessage) {
            duplicateCheck.compute(cleanedMessage, (msg, n) -> {
                long _n = null != n ? n : 0;
                _n = _n + 1;
                return _n;
            });
        }

        final boolean static_literal = result.metaData.has("static_literal") ? result.metaData.get("static_literal").getAsBoolean() : false;
        final String type = result.metaData.get("type").getAsString();

        if ("LOG_OUTPUT".equals(type)) {
            final String logLevel = result.metaData.has("logger_level") ? result.metaData.get("logger_level").getAsString() : null;
            if (null != logLevel) {
                logByLogLevel.compute(logLevel, (lvl, n) -> {
                    long _n = null != n ? n : 0;
                    _n = _n + 1;
                    return _n;
                });
            }

            if (static_literal) {
                ++n_log_static;
            } else {
                ++n_log_dynamic;
            }
        }
        else if ("EXCEPTION_THROW".equals(type)) {
            if (static_literal) {
                ++n_exception_static;
            } else {
                ++n_exception_dynamic;
            }
        }
    }

    public final void processResults() {
        results.forEach(result -> {
            JsonElement cleanedMessage = result.metaData.get("cleaned_message");
            if (null != cleanedMessage) {
                long n = duplicateCheck.get(cleanedMessage.getAsString());
                final String type = result.metaData.get("type").getAsString();

                if (n > 1) {
                    if ("LOG_OUTPUT".equals(type)) {
                        ++n_log_duplicate;
                    } else if ("EXCEPTION_THROW".equals(type)) {
                        ++n_exception_duplicate;
                    }
                }
            }
        });
    }

    public Throwable throwable = null;

    public final JsonObject toRunMetadata(boolean exitSuccess) {
        final java.util.Date end = new Date();

        JsonObject runMetadata = new JsonObject();

        long n_new = newErrorOrdinals.size();
        long n_existing = existingErrorOrdinals.size();
        long n_abandoned = abandonedErrorOrdinals.size();

        long n_new_log = newErrorOrdinalsInLog.size();
        long n_existing_log = existingErrorOrdinalsInLog.size();

        long n_new_ex = newErrorOrdinalsInException.size();
        long n_existing_ex = existingErrorOrdinalsInException.size();
        
        long n_new_ph = newErrorOrdinalsInPlaceholder.size();
        long n_existing_ph = existingErrorOrdinalsInPlaceholder.size();

        runMetadata.addProperty("n_new", n_new);
        runMetadata.addProperty("n_existing", n_existing);
        runMetadata.addProperty("n_total", n_new + n_existing);
        runMetadata.addProperty("n_abandoned", n_abandoned);

        JsonArray ary = new JsonArray();
        JsonArray finalAry = ary;
        newErrorOrdinals.forEach(l -> { finalAry.add(l); });
        runMetadata.add("new", ary);

        ary = new JsonArray();
        JsonArray finalAry1 = ary;
        existingErrorOrdinals.forEach(l -> { finalAry1.add(l);});
        runMetadata.add("existing", ary);

        ary = new JsonArray();
        JsonArray finalAry2 = ary;
        abandonedErrorOrdinals.forEach(l -> { finalAry2.add(l); });
        runMetadata.add("abandoned", ary);

        runMetadata.addProperty("n_new_log", n_new_log);
        runMetadata.addProperty("n_existing_log", n_existing_log);
        runMetadata.addProperty("n_total_log", n_new_log + n_existing_log);

        runMetadata.addProperty("n_new_ex", n_new_ex);
        runMetadata.addProperty("n_existing_ex", n_existing_ex);
        runMetadata.addProperty("n_total_ex", n_new_ex + n_existing_ex);

        runMetadata.addProperty("n_new_ph", n_new_ph);
        runMetadata.addProperty("n_existing_ph", n_existing_ph);
        runMetadata.addProperty("n_total_ph", n_new_ph + n_existing_ph);

        // average # codes per file
        AtomicLong i = new AtomicLong();
        AtomicLong n = new AtomicLong();
        AtomicLong i_lines = new AtomicLong();
        errorsPerFile.forEach((filename, n_errors) -> {
            i.incrementAndGet();
            n.addAndGet(n_errors);

            long n_lines = linesPerFile.get(filename);
            i_lines.addAndGet(n_lines);
        });

        runMetadata.addProperty("number_of_files", i.get());
        if (n.get() != 0) {
            runMetadata.addProperty("ratio_lines_per_error", ((double) i_lines.get()) / ((double) n.get()));
        }
        if (i.get() != 0) {
            runMetadata.addProperty("average_errors_per_file", ((double) n.get()) / ((double) i.get()));
        }
        runMetadata.addProperty("run_duration_seconds", (end.getTime()-start.getTime())/1000L);

        runMetadata.addProperty("start_ts", start.getTime());
        runMetadata.addProperty("end_ts", end.getTime());

        if (null != throwable) {
            runMetadata.addProperty("exit_status", "failure");
            runMetadata.addProperty("exit_error_message", throwable.getMessage());
        } else {
            runMetadata.addProperty("exit_status", exitSuccess ? "success" : "failure");
        }

        // log-by-log-level
        JsonObject logByLogLevel = new JsonObject();
        runMetadata.add("n_log_by_log_level", logByLogLevel);
        this.logByLogLevel.forEach((level, _n) -> {
            logByLogLevel.addProperty(level, _n);
        });

        runMetadata.addProperty("n_log_static", n_log_static);
        runMetadata.addProperty("n_log_dynamic", n_log_dynamic);
        runMetadata.addProperty("n_log_duplicate", n_log_duplicate);

        runMetadata.addProperty("n_exception_static", n_exception_static);
        runMetadata.addProperty("n_exception_dynamic", n_exception_dynamic);
        runMetadata.addProperty("n_exception_duplicate", n_exception_duplicate);

        return runMetadata;
    }
}
