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
     * Method comment
     */
    void method1() {

        logger.trace("Starting...");

        var foo = false;
        if (foo) {
            b.method2();
        }

        logger.trace("Finishing...");
    }
}