<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        \an_example("This isn't an error.");

        _l("[E-1] This is an error.");

        error_log('This isn\'t an error.');

        error_log('[E-2] ERROR\tThis is an error.');

        throw new \Exception("[E-3] This is an exception.");

        throw new \Exception('[E-4] This is an exception.');

    }

    public function problem_function($param1 = 'default') {
        _l('[E-5] This is an error.');
    }

}