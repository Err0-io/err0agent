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

package io.err0.client.test;

import io.err0.client.core.*;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UnitTestApiProvider implements ApiProvider {
    @Override
    public void close() {

    }

    @Override
    public void ensurePolicyIsSetUp(ProjectPolicy policy) {

    }

    @Override
    public void cacheAllValidErrorNumbers(ProjectPolicy policy) {
        // does nothing
    }

    @Override
    public void clearErrorNumberCache(ProjectPolicy policy) {
        // does nothing
    }

    @Override
    public boolean markRenumberingOK(ProjectPolicy policy) {
        return true; // carry on...
    }

    @Override
    public boolean validErrorNumber(final ProjectPolicy policy, final long errorCode) {
        return true;
    }

    public void clearHistory() {
        currentErrorNumber = 0;
        metaDataStorage.clear();
        resultStorage.clear();
    }

    private long currentErrorNumber = 0;

    @Override
    public long nextErrorNumber(final ProjectPolicy policy) {
        return ++currentErrorNumber;
    }

    @Override
    public void cacheErrorNumberBatch(ProjectPolicy policy, long number) {
        // do nothing
    }

    @Override
    public void setNextErrorNumber(final ProjectPolicy policy, final long nextErrorNumber) {
        currentErrorNumber = nextErrorNumber-1;
    }

    public static class MetaData {
        MetaData(final String errorCode, final long errorOrdinal, final JsonObject metaData) {
            this.errorCode = errorCode;
            this.errorOrdinal = errorOrdinal;
            this.metaData = metaData;
        }
        final String errorCode;
        final long errorOrdinal;
        final JsonObject metaData;
    }

    public HashMap<Long, MetaData> metaDataStorage = new HashMap<>();

    @Override
    public void bulkInsertMetaData(final ProjectPolicy policy, final UUID run_uuid, final String errorPrefix, final ArrayList<ForInsert> forBulkInsert) {
        forBulkInsert.forEach(forInsert -> {
            metaDataStorage.put(forInsert.errorOrdinal, new MetaData(forInsert.errorCode, forInsert.errorOrdinal, forInsert.metaData));
        });
    }

    public static class Result {
        Result(final boolean changed, final String path, final String sourceCode) {
            this.changed = changed;
            this.path = path;
            this.sourceCode = sourceCode;
        }
        public final boolean changed;
        public final String path;
        public final String sourceCode;
    }

    public HashMap<String, Result> resultStorage = new HashMap<>();

    class UnitTestResultDriver implements ResultDriver {
        @Override
        public void processResult(boolean changed, String path, SourceCodeParse parse, Charset charset) {

            String filename = path;
            int n = filename.lastIndexOf('/');
            if (n >= 0) { filename = filename.substring(n+1); }

            final StringBuilder output = new StringBuilder();
            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                output.append(parse.tokenList.get(j).source);
            }

            resultStorage.put(filename, new Result(changed, path, output.toString()));

        }
    }

    ResultDriver driver = new UnitTestResultDriver();
    public ResultDriver getDriver() { return driver; }

    public void writeResultsTo(final String targetPath) {
        resultStorage.forEach((filename, result) -> {
            try {
                Utils.writeString(Utils.pathOf(targetPath + "/" + filename), result.sourceCode);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public UUID createRun(ProjectPolicy policy) {
        return createRun(policy, new JsonObject(), new JsonObject(), "insert");
    }
    
    @Override
    public UUID createRun(ProjectPolicy policy, JsonObject appGitMetadata, JsonObject runGitMetadata, final String runState) {
        return UUID.randomUUID();
    }

    @Override
    public void updateRun(ProjectPolicy policy, UUID run_uuid, JsonObject gitMetadata, JsonObject runMetadata) {
        // do nothing
    }

    public static class UnitTestState {
        private UnitTestState(UnitTestApiProvider provider) {
            this.currentErrorNumber = provider.currentErrorNumber;
            this.metaDataStorage = new HashMap<>();
            provider.metaDataStorage.forEach((errorOrdinal, metaData) -> {
                this.metaDataStorage.put(errorOrdinal, metaData);
            });
        }
        public final long currentErrorNumber;
        public final HashMap<Long, MetaData> metaDataStorage;
        public final void transferSignatures(GlobalState globalState) {
            globalState.previousRunSignatures.clear();
            metaDataStorage.forEach((errorOrdinal, metaData) -> {
                globalState.previousRunSignatures.put(errorOrdinal, new Signature(metaData.metaData));
            });
        }
    }

    public UnitTestState getState() {
        return new UnitTestState(this);
    }

    @Override
    public void importPreviousState(final ProjectPolicy policy, final GlobalState globalState, final String currentBranch) {
        // does nothing, unit tests work slightly differently to the database
        // methods.
    }

    @Override
    public void finaliseRun(ProjectPolicy policy, UUID run_uuid) {
        // does nothing
    }
}
