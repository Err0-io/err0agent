<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        \an_example("This isn't an error.");

        error_log("This is an error.");

        error_log('This is an error.');

        throw new \Exception("This is an exception.");

        throw new \Exception('This is an exception.');

    }

    public function problem_function($param1 = 'default') {
        error_log('This is an error.');
    }

}