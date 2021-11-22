package io.err0.testdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A class to test the log4j api, java parser.
 */
public class B {

    private static final Logger logger = LogManager.getLogger(B.class);

    /**
     * Method comment
     */
    void method1() {

        throw new RuntimeException("[E-6] Not implemented.");
    }
}