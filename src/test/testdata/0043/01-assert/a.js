// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('[E-1] a log message');
        console.warn("[E-2] a warning to you");
        console.error('[E-3] an error condition');
    }

    const data = {
        "key": "E-4",
        "value": 1,
    };

    throw new Error('[E-5] oops!  an error occurred.');
});

class A {
    
    logger = LoggerFactory.getLogger();
    
    get a_getter() {
        let foo = false;

        logger.debug('a debug message');

        const str = 'Not a __PLACEHOLDER__';

        if (foo) {
            logger.log('[E-6] a log message');
            logger.warn("[E-7] a warning to you");
            logger.error('[E-8] an error condition');

            throw new Error('[E-9] oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        logger.debug('a debug message');

        if (foo) {
            logger.log('[E-10] a log message');
            logger.warn("[E-11] a warning to you");
            logger.error('[E-12] an error condition');

            throw new Error('[E-13] oops!  an error occurred.');
        } else {
            const str = "E-14";
            throw str;
        }

        return foo;
    }
}