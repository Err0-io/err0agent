<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        \an_example("This isn't an error.");

        error_log("[E-1] This is an error.");

        error_log('[E-2] This is an error.');

        throw new \Exception("[E-3] This is an exception.");

        throw new \Exception('[E-4] This is an exception.');

    }

    public function problem_function($param1 = 'default') {
        error_log('[E-5] This is an error.');
    }

}