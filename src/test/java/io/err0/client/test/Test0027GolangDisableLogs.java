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

/**
 * Tests the standard go library "errors.New"
 */
public class Test0027GolangDisableLogs {
    @Test
    public void t0001InjectErrorCodes() {

        final String sourceDir = "src/test/testdata/0027/01";
        final String assertDir = "src/test/testdata/0027/01-assert";

        final ProjectPolicy policy = TestPolicy.getPolicyDisableLogs();
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

        apiProvider.resultStorage.forEach((filename, result) -> {
            try {
                final String expectedSourceCode = Utils.readString(Utils.pathOf(assertDir + "/" + filename));
                assertEquals(expectedSourceCode, result.sourceCode, filename);
            }
            catch (IOException e) {
                fail(e);
            }
        });

        assertEquals(globalState.files.size(), apiProvider.resultStorage.size());

        {
            // assert regarding ProblemMethod
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(2l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(1, array.size());
            assertEquals("func (p *ExampleType) exampleFunc(src interface{}) error", array.get(0).getAsJsonObject().get("c").getAsString());
        }
    }
}
