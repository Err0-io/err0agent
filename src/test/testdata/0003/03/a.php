<?php

/*
 * do you think anyone, anywhere, uses function x(args) use (outofscope) { } syntax?
 */

$global = 'hello, ';

$anonymous_function = function ($bar) use ($global) {
    echo $global . $bar . "\n";
    error_log("check the signature of this method.");
};

$anonymous_function('world');