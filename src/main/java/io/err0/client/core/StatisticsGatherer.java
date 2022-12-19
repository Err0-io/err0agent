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

    public final HashSet<Long> abandonedErrorOrdinals = new HashSet<>();

    public final HashMap<String, Long> linesPerFile = new HashMap<>();
    public final HashMap<String, Long> errorsPerFile = new HashMap<>();

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

        return runMetadata;
    }
}
