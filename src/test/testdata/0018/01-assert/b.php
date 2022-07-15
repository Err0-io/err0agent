<?php

namespace \io\err0\testdata;

class example extends \nonexistant {

    public function example_function($param1, $param2) {

        error_log('[E-3]');

        throw new \Exception("[E-4]");

    }

}