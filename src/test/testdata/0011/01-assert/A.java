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

        System.out.println("[E-1] ERROR\tStarting...");

        var foo = false;
        if (foo) {
            _l("[E-2] foo is true.");

            throw new RuntimeException("[E-3] This is an exception");
        }

        _l("[E-4] Finishing...");
    }
}