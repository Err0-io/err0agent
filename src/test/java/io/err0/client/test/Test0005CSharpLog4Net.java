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

import com.google.gson.JsonArray;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.GlobalState;
import io.err0.client.Main;
import io.err0.client.core.ResultDriver;
import io.err0.client.core.StatisticsGatherer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import io.err0.client.core.Utils;

public class Test0005CSharpLog4Net {
    @Test
    public void t0001InjectErrorCodes() {

        final String sourceDir = "src/test/testdata/0005/01";
        final String assertDir = "src/test/testdata/0005/01-assert";

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
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(6l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(2, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            assertEquals("[Annotation(val=\"1\")] public void ProblemMethod(string param = \"default\")", array.get(1).getAsJsonObject().get("c").getAsString());
        }

        {
            // assert regarding ProblemMethod
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(7l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(2, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            assertEquals("[Annotation(val=\"2\")] public void ProblemMethod(string param = \"default\")", array.get(1).getAsJsonObject().get("c").getAsString());
        }

        {
            // assert regarding [HttpGet] attribute on GetInformation()
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(1l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(3, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            assertEquals("[HttpGet] public object GetInformation()", array.get(1).getAsJsonObject().get("c").getAsString());
            assertEquals("if (foo)", array.get(2).getAsJsonObject().get("c").getAsString());
        }

        {
            // assert regarding lambda expressions rather than lambda functions
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(8l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(1, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            //FIXME: assertEquals("public static ReturnValue MethodDeclaredAsLambdaExpression(this ReturnValue input)", array.get(1).getAsJsonObject().get("c").getAsString());
            // ^- can't work because call stack routines imagine going "up" a depth level until the "top" - here the lambda expression is on the same
            //    depth level as the exception...
        }

        {
            // assert regarding [HttpGet] attribute on GetInformation()
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(1l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(3, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            assertEquals("[HttpGet] public object GetInformation()", array.get(1).getAsJsonObject().get("c").getAsString());
            assertEquals("if (foo)", array.get(2).getAsJsonObject().get("c").getAsString());
        }

        {
            // assert regarding #endregion #region issue
            UnitTestApiProvider.MetaData r1 = apiProvider.metaDataStorage.get(9l);
            assertNotNull(r1);
            JsonArray array = r1.metaData.getAsJsonArray("methods");
            assertEquals(2, array.size());
            assertEquals("[Attribute(parameter = \"value\")] public class A", array.get(0).getAsJsonObject().get("c").getAsString());
            assertEquals("private static Object AMethod(string aParameter)", array.get(1).getAsJsonObject().get("c").getAsString());
        }
    }
}
