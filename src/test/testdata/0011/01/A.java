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

        System.out.println("ERROR\tStarting...");

        var foo = false;
        if (foo) {
            _l("foo is true.");

            throw new RuntimeException("This is an exception");
        }

        _l("Finishing...");
    }
}