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

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.GlobalState;
import io.err0.client.Main;
import io.err0.client.core.ResultDriver;
import io.err0.client.core.StatisticsGatherer;
import io.err0.client.core.Utils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Test0001JavaLog4j {
    @Test
    public void t0001InjectErrorCodes() {

        UnitTestApiProvider.UnitTestState previousState = null;

        // pass #1 - scan and analyse
        {

            final String sourceDir = "src/test/testdata/0001/01";
            final String assertDir = "src/test/testdata/0001/01-assert";

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

            final String sourceDir = "src/test/testdata/0001/02";

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

            assertEquals(8, previousState.metaDataStorage.size());

            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(7L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(2, array.size());
                assertEquals("public class A", array.get(0).getAsJsonObject().get("c").getAsString());
                assertEquals(9L, array.get(0).getAsJsonObject().get("l").getAsLong());
                assertEquals("void method2( String parameter )", array.get(1).getAsJsonObject().get("c").getAsString());
                assertEquals(46L, array.get(1).getAsJsonObject().get("l").getAsLong());
            }
            {
                UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(8L);
                assertNotNull(r1);
                JsonArray array = r1.metaData.getAsJsonArray("methods");
                assertEquals(4, array.size());
                assertEquals("public class A", array.get(0).getAsJsonObject().get("c").getAsString());
                assertEquals(9L, array.get(0).getAsJsonObject().get("l").getAsLong());
                assertEquals("class", array.get(0).getAsJsonObject().get("t").getAsString());
                assertEquals("protected void checkParameter(String param)", array.get(1).getAsJsonObject().get("c").getAsString());
                assertEquals(50L, array.get(1).getAsJsonObject().get("l").getAsLong());
                assertEquals("method", array.get(1).getAsJsonObject().get("t").getAsString());
                assertEquals("if (\"const\".equals(param))", array.get(2).getAsJsonObject().get("c").getAsString());
                assertEquals(52L, array.get(2).getAsJsonObject().get("l").getAsLong());
                assertEquals("control", array.get(2).getAsJsonObject().get("t").getAsString());
                assertEquals("if (a == null && b == null)", array.get(3).getAsJsonObject().get("c").getAsString());
                assertEquals(53L, array.get(3).getAsJsonObject().get("l").getAsLong());
                assertEquals("control", array.get(3).getAsJsonObject().get("t").getAsString());
            }
        }

        // pass #3 - scan and report (changes)
        {

            final String sourceDir = "src/test/testdata/0001/03";

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

            assertTrue(wouldChangeAFile);

            previousState = apiProvider.getState();

            assertEquals(8, previousState.metaDataStorage.size());
        }
    }
}
