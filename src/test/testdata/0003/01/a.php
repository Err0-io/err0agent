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

    public function typed_function(string $param1 = 'this') : string {
        error_log('A log');
        return $param1;
    }

    private function another_typed_function(bool $truth):bool{
        error_log('Another log');
        return $truth;
    }

}