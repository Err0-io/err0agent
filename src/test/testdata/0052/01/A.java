package io.err0.testdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Controller
@RequestMapping("/route/{anId}")
class A {

    private static final Logger logger = LogManager.getLogger(A.class);

    @ModelAttribute("attr")
    void method1(@PathVariable("anId") int id) {
        throw new RuntimeException("An error...");
    }
}