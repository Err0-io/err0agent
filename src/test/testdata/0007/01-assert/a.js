// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('[E-1] a log message');
        console.warn("[E-2] a warning to you");
        console.error('[E-3] an error condition');
    }

    throw new Error('[E-4] oops!  an error occurred.');
});

class A {
    get a_getter() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('[E-5] a log message');
            console.warn("[E-6] a warning to you");
            console.error('[E-7] an error condition');

            throw new Error('[E-8] oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('[E-9] a log message');
            console.warn("[E-10] a warning to you");
            console.error('[E-11] an error condition');

            throw new Error('[E-12] oops!  an error occurred.');
        }

        return foo;
    }
}