// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('a log message');
        console.warn("a warning to you");
        console.error('an error condition');
    }

    throw new Error('oops!  an error occurred.');
});

class A {
    get a_getter() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('a log message');
            console.warn("a warning to you");
            console.error('an error condition');

            throw new Error('oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        console.debug('a debug message');

        if (foo) {
            console.log('a log message');
            console.warn("a warning to you");
            console.error('an error condition');

            throw new Error('oops!  an error occurred.');
        }

        return foo;
    }
}