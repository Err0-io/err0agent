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

        var foo = false;
        if (foo) {
            logger.debug("foo is true.");

            logger.info("[E-1] info level");
            logger.warn("[E-2] warn level");
            logger.error("[E-3] error level");

            // this is always fatal
            logger.fatal("[E-4] fatal level");

            throw new RuntimeException("[E-5] This is an exception");
        }

        logger.trace("Finishing...");
    }
}