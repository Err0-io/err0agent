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

import io.err0.client.Main;
import io.err0.client.core.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Test0054Swift {
    @Test
    public void t0001InjectErrorCodes() {

        UnitTestApiProvider.UnitTestState previousState = null;

        // pass #1 - scan and analyse
        {

            final String sourceDir = "src/test/testdata/0054/01";
            final String assertDir = "src/test/testdata/0054/01-assert";

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

            // check static literal
            {
                UnitTestApiProvider.MetaData metaData = previousState.metaDataStorage.get(1L);
                assertNotNull(metaData);
                boolean static_literal = metaData.metaData.get("static_literal").getAsBoolean();
                assertTrue(static_literal);
            }

            // check dynamic literal
            {
                UnitTestApiProvider.MetaData metaData = previousState.metaDataStorage.get(2L);
                assertNotNull(metaData);
                boolean static_literal = metaData.metaData.get("static_literal").getAsBoolean();
                assertFalse(static_literal);
            }

            {
                UnitTestApiProvider.MetaData metaData = previousState.metaDataStorage.get(3L);
                assertNotNull(metaData);
                boolean static_literal = metaData.metaData.get("static_literal").getAsBoolean();
                assertTrue(static_literal);
            }

            {
                UnitTestApiProvider.MetaData metaData = previousState.metaDataStorage.get(4L);
                assertNotNull(metaData);
                boolean static_literal = metaData.metaData.get("static_literal").getAsBoolean();
                assertFalse(static_literal);
            }

            {
                UnitTestApiProvider.MetaData metaData = previousState.metaDataStorage.get(5L);
                assertNotNull(metaData);
                boolean static_literal = metaData.metaData.get("static_literal").getAsBoolean();
                assertTrue(static_literal);
            }
        }

        // pass #2 - scan and report (no changes)
        {

            final String sourceDir = "src/test/testdata/0054/02";

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

            assertEquals(13, previousState.metaDataStorage.size());
        }

        // pass #3 - insert
        {

            final String sourceDir = "src/test/testdata/0054/03";
            final String assertDir = "src/test/testdata/0054/03-assert";

            final ProjectPolicy policy = TestPolicy.getPolicy();
            assertNotNull(policy);

            final GlobalState globalState = new GlobalState();
            assertNotNull(globalState);

            final UnitTestApiProvider apiProvider = new UnitTestApiProvider();
            final ResultDriver driver = apiProvider.getDriver();

            Main.scan(policy, globalState, sourceDir, apiProvider, false);
            Main._import(apiProvider, globalState, policy);
            boolean didChangeAFile = Main.runInsert(apiProvider, globalState, policy, driver, apiProvider.createRun(policy), new StatisticsGatherer());

            assertFalse(didChangeAFile);

            // output the results to 01-assert
            // apiProvider.writeResultsTo(assertDir);

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

            assertEquals(13, previousState.metaDataStorage.size());
        }
    }
}
