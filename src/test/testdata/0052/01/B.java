package io.err0.testdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class B {

    private static final Logger logger = LogManager.getLogger(B.class);

    /** a comment */
    B exampleMethod(String parameter) {
        throw new RuntimeException("An error...");
    }

    @Example('\"')
    void exampleMethod2() {
        throw new RuntimeException("An error...");
    }
}