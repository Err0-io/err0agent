<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        \an_example("This isn't an error.");

        error_log("[E-1] This is an error.");

        error_log('[E-2] This is an error.');

        throw new \Exception("[E-3] This is an exception.");

        throw new \Exception('[E-4] This is an exception.');

        $var1 = '__PLACEHOLDER__ without replacement.';
        $var2 = 'E-5';
        $var3 = "E-6";

    }

    public function problem_function($param1 = 'default') {
        error_log('[E-7] This is an error.');
    }

}