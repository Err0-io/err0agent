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

            logger.info("[E-1] info level");
            logger.warn("[E-2] warn level");

            // with slf4j error is fatal.
            logger.error("[E-3] error level");

            throw new RuntimeException("[E-4] This is an exception");
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

            logger.atInfo().addArgument(foo).log("[E-5] foo is {}.");
            logger.atWarn().addArgument(foo).log("[E-6] foo is {}.");
            logger.atError().addArgument(foo).log("[E-7] foo is {}.");

            logger.atInfo().addArgument("a").log("[E-8] foo is {}.");
            logger.atWarn().addArgument("b").log("[E-9] foo is {}.");
            logger.atError().addArgument("c").log("[E-10] foo is {}.");

            throw new RuntimeException("[E-11] This is an exception")
        }

        logger.atTrace().addArgument((new java.util.Date()).getTime()).log("Finishing @ {} ...");

    }
}