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

package io.err0.client.test;

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
        //apiProvider.writeResultsTo(assertDir);

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

        // assert regarding problem_function
        //UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(5l);
        //assertNotNull(r1);
        //JsonArray array = r1.metaData.getAsJsonArray("methods");
        //assertEquals(2, array.size());
        //assertEquals("public function problem_function($param1 = 'default')", array.get(1).getAsJsonObject().get("c").getAsString());
    }
}
