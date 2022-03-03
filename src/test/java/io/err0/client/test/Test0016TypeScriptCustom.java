package io.err0.client.test;

import io.err0.client.Main;
import io.err0.client.core.GlobalState;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.ResultDriver;
import io.err0.client.core.StatisticsGatherer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Test0016TypeScriptCustom {
    @Test
    public void t0001InjectErrorCodes() {

        final String sourceDir = "src/test/testdata/0016/01";
        final String assertDir = "src/test/testdata/0016/01-assert";

        final ProjectPolicy policy = TestPolicy.getAdvTypeScriptPolicy();
        assertNotNull(policy);

        final GlobalState globalState = new GlobalState();
        assertNotNull(globalState);

        final UnitTestApiProvider apiProvider = new UnitTestApiProvider();
        final ResultDriver driver = apiProvider.getDriver();

        Main.scan(policy, globalState, sourceDir, apiProvider);
        Main._import(apiProvider, globalState, policy);
        Main.runInsert(apiProvider, globalState, policy, driver, apiProvider.createRun(policy), new StatisticsGatherer());

        // output the results to 01-assert
        // apiProvider.writeResultsTo(assertDir);

        apiProvider.resultStorage.forEach((filename, result) -> {
           try {
               final String expectedSourceCode = Files.readString(Path.of(assertDir + "/" + filename));
               assertEquals(expectedSourceCode, result.sourceCode, filename);
           }
           catch (IOException e) {
               fail(e);
           }
        });

        assertEquals(globalState.files.size(), apiProvider.resultStorage.size());
    }
}
