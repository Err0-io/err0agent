package io.err0.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to test the log4j api, java parser.
 */
public class A {

    private static final Logger logger = LoggerFactory.getLogger(A.class);

    /**
     * Using the 'classical' api of slf4j
     */
    void slf4j_classical_api() {

        logger.trace("Starting...");

        var foo = false;
        if (foo) {
            logger.debug("foo is true.");

            logger.info("info level");
            logger.warn("warn level");

            // with slf4j error is fatal.
            logger.error("error level");

            throw new RuntimeException("This is an exception");
        }

        logger.trace("Finishing...");
    }

    /**
     * Using the 'fluent' api of slf4j
     */
    void slf4j_fluent_api() {

        logger.atTrace().addArgument((new java.util.Date()).getTime()).log("Starting @ {} ...");

        var foo = false;
        if (foo) {
            logger.atDebug().addArgument(foo).log("foo is {}.");

            logger.atInfo().addArgument(foo).log("foo is {}.");
            logger.atWarn().addArgument(foo).log("foo is {}.");
            logger.atError().addArgument(foo).log("foo is {}.");

            logger.atInfo().addArgument("a").log("foo is {}.");
            logger.atWarn().addArgument("b").log("foo is {}.");
            logger.atError().addArgument("c").log("foo is {}.");

            throw new RuntimeException("This is an exception")
        }

        logger.atTrace().addArgument((new java.util.Date()).getTime()).log("Finishing @ {} ...");

    }
}