<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        \an_example("This isn't an error.");

        _l("This is an error.");

        error_log('This isn\'t an error.');

        error_log('ERROR\tThis is an error.');

        throw new \Exception("This is an exception.");

        throw new \Exception('This is an exception.');

    }

    public function problem_function($param1 = 'default') {
        _l('This is an error.');
    }

}