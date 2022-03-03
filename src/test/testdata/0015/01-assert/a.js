// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('a log message');
        _l("[E-1] a warning to you");
        console.error('[E-2] ERROR! an error condition');
    }

    throw new Error('[E-3] oops!  an error occurred.');
});

class A {
    get a_getter() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('a log message');
            _l("[E-4] a warning to you");
            console.error('[E-5] ERROR! an error condition');

            throw new Error('[E-6] oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('a log message');
            _l("[E-7] a warning to you");
            console.error('[E-8] ERROR! an error condition');

            throw new Error('[E-9] oops!  an error occurred.');
        }

        return foo;
    }
}