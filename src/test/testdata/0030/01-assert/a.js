// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('[E-1] a log message');
        console.warn("[E-2] a warning to you");
        console.error('[E-3] an error condition');
    }

    throw new Error('oops!  an error occurred.');
});

class A {
    
    logger = LoggerFactory.getLogger();
    
    get a_getter() {
        let foo = false;

        logger.debug('a debug message');

        if (foo) {
            logger.log('[E-4] a log message');
            logger.warn("[E-5] a warning to you");
            logger.error('[E-6] an error condition');

            throw new Error('oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        logger.debug('a debug message');

        if (foo) {
            logger.log('[E-7] a log message');
            logger.warn("[E-8] a warning to you");
            logger.error('[E-9] an error condition');

            throw new Error('oops!  an error occurred.');
        }

        return foo;
    }
}