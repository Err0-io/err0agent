package io.err0.testdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A class to test the log4j api, java parser.
 */
public class A {

    private static final Logger logger = LogManager.getLogger(A.class);

    /**
     * Method comment
     */
    void method1() {

        logger.trace("Starting...");

        object.method("Don't add error code");

        String dontReplace = "A string __PLACEHOLDER__ not to replace...";

        String withPlaceholder = "E-1";

        var foo = false;
        if (foo) {
            logger.debug("foo is true.");

            logger.info("[E-2] info level");
            logger.warn("[E-3] warn level");
            logger.error("[E-4] error level");

            logger.error("""
                         [E-5] Multi-line string.
                         Line 2.
                         """);

            // this is always fatal
            logger.fatal("[E-6] fatal level");

            throw new RuntimeException("[E-7] This is an exception");
        }

        String withPlaceholder2 = "E-8"; // copy-paste-duplication.

        logger.trace("Finishing...");
    }
}