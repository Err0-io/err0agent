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
import io.err0.client.core.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Test0053CSharpDotNetCore {
    @Test
    public void t0001InjectErrorCodes() {

        UnitTestApiProvider.UnitTestState previousState = null;

        {

            final String sourceDir = "src/test/testdata/0053/01";
            final String assertDir = "src/test/testdata/0053/01-assert";

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

        assertEquals(4, previousState.metaDataStorage.size());

        {
            UnitTestApiProvider.MetaData r1 = previousState.metaDataStorage.get(1L);
            assertNotNull(r1);
            assertEquals("Information", r1.metaData.get("logger_level").getAsString());
        }
        {
            UnitTestApiProvider.MetaData r1 = previousState.metaDataStorage.get(2L);
            assertNotNull(r1);
            assertEquals("Warning", r1.metaData.get("logger_level").getAsString());
        }
        {
            UnitTestApiProvider.MetaData r1 = previousState.metaDataStorage.get(3L);
            assertNotNull(r1);
            assertEquals("Error", r1.metaData.get("logger_level").getAsString());
        }
        {
            UnitTestApiProvider.MetaData r1 = previousState.metaDataStorage.get(4L);
            assertNotNull(r1);
            assertEquals("Critical", r1.metaData.get("logger_level").getAsString());
        }
    }
}
