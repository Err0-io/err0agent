package io.err0.testdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A class to test the log4j api, java parser.
 */
public class A {

    private static final Logger logger = LogManager.getLogger(A.class);

    private B b = new B();

    /**
     * I don't want these error codes to stay the same after re-running the error tool.
     * A cut and paste job, changing the method signature.
     */
    void method2() {

        b.method1();

        logger.trace("Starting...");

        var foo = false;
        if (foo) {
            logger.debug("foo is true.");

            logger.info("[E-7] info level");
            logger.warn("[E-8] warn level");
            logger.error("[E-9] error level");

            // this is always fatal
            logger.fatal("[E-10] fatal level");

            throw new RuntimeException("[E-11] This is an exception");
        }

        logger.trace("Finishing...");
    }
}