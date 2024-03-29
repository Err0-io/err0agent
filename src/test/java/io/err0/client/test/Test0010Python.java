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

import com.google.gson.JsonArray;
import io.err0.client.Main;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.GlobalState;
import io.err0.client.core.ResultDriver;
import io.err0.client.core.StatisticsGatherer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import io.err0.client.core.Utils;

public class Test0010Python {
    @Test
    public void t0001InjectErrorCodes() {

        UnitTestApiProvider.UnitTestState previousState = null;

        // pass #1 - insert codes
        {

            final String sourceDir = "src/test/testdata/0010/01";
            final String assertDir = "src/test/testdata/0010/01-assert";

            final ProjectPolicy policy = TestPolicy.getPolicy();
            assertNotNull(policy);

            final GlobalState globalState = new GlobalState();
            assertNotNull(globalState);

            final UnitTestApiProvider apiProvider = new UnitTestApiProvider();
            final ResultDriver driver = apiProvider.getDriver();

            Main.scan(policy, globalState, sourceDir, apiProvider, false);
            Main._import(apiProvider, globalState, policy);
            Main.runInsert(apiProvider, globalState, policy, driver, apiProvider.createRun(policy), new StatisticsGatherer());

            // output the results to 01-assert
            // apiProvider.writeResultsTo(assertDir);
            // System.exit(-1);

            apiProvider.resultStorage.forEach((filename, result) -> {
                try {
                    final String expectedSourceCode = Utils.readString(Utils.pathOf(assertDir + "/" + filename));
                    assertEquals(expectedSourceCode, result.sourceCode, filename);
                } catch (IOException e) {
                    fail(e);
                }
            });

            assertEquals(globalState.files.size(), apiProvider.resultStorage.size());

            previousState = apiProvider.getState();
        }

        // pass #2 - scan and report (no changes)
        {

            final String sourceDir = "src/test/testdata/0010/02";

            final ProjectPolicy policy = TestPolicy.getPolicy();
            assertNotNull(policy);

            final GlobalState globalState = new GlobalState();
            assertNotNull(globalState);

            final UnitTestApiProvider apiProvider = new UnitTestApiProvider();

            previousState.transferSignatures(globalState);
            apiProvider.setNextErrorNumber(policy, previousState.currentErrorNumber + 1);

            final ResultDriver driver = apiProvider.getDriver();

            Main.scan(policy, globalState, sourceDir, apiProvider, false);
            Main._import(apiProvider, globalState, policy);
            boolean wouldChangeAFile = Main.runAnalyse(apiProvider, globalState, policy, driver, apiProvider.createRun(policy), new StatisticsGatherer());

            assertFalse(wouldChangeAFile);

            previousState = apiProvider.getState();

            assertEquals(11, previousState.metaDataStorage.size());
            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(8L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(2, array.size());
                assertEquals("class Example:", array.get(0).getAsJsonObject().get("c").getAsString());
                assertEquals(23L, array.get(0).getAsJsonObject().get("l").getAsLong());
                assertEquals("def continuation(", array.get(1).getAsJsonObject().get("c").getAsString());
                assertEquals(48L, array.get(1).getAsJsonObject().get("l").getAsLong());
            }
            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(9L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(1, array.size());
                assertEquals("def another_continuation(", array.get(0).getAsJsonObject().get("c").getAsString());
            }
            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(10L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(3, array.size());
                assertEquals("class NotSpaced(object):", array.get(0).getAsJsonObject().get("c").getAsString());
                assertEquals(58L, array.get(0).getAsJsonObject().get("l").getAsLong());
                assertEquals("def __init__(self, mode, device, dtype):", array.get(1).getAsJsonObject().get("c").getAsString());
                assertEquals(59L, array.get(1).getAsJsonObject().get("l").getAsLong());
                assertEquals("else:", array.get(2).getAsJsonObject().get("c").getAsString());
                assertEquals(62L, array.get(2).getAsJsonObject().get("l").getAsLong());
            }
            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(11L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(2, array.size());
                assertEquals("class NotSpaced(object):", array.get(0).getAsJsonObject().get("c").getAsString());
                assertEquals(58L, array.get(0).getAsJsonObject().get("l").getAsLong());
                assertEquals("def trouble():", array.get(1).getAsJsonObject().get("c").getAsString());
                assertEquals(64L, array.get(1).getAsJsonObject().get("l").getAsLong());
            }
        }
    }
}
