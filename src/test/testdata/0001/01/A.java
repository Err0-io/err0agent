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

            logger.info("info level");
            logger.warn("warn level");
            logger.error("error level");

            logger.error("""
                         Multi-line string.
                         Line 2.
                         """);

            // this is always fatal
            logger.fatal("fatal level");

            throw new RuntimeException("This is an exception");
        }

        logger.trace("Finishing...");
    }

    void method2(
            String parameter
    ) {
        logger.info("A log")
    }

    protected void checkParameter(String param) {
        String a = null, b = null;
        if ("const".equals(param)) {
            if (a == null && b == null) {
                throw new RuntimeException("Example call stack issue.");
            }
        }
    }
}