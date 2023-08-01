// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('a log message');
        console.warn("a warning to you");
        console.error('an error condition');
    }

    const data = {
        "key": "__PLACEHOLDER__",
        "value": 1,
    };

    throw new Error('oops!  an error occurred.');
});

class A {
    
    logger = LoggerFactory.getLogger();
    
    get a_getter() {
        let foo = false;

        logger.debug('a debug message');

        const str = 'Not a __PLACEHOLDER__';

        if (foo) {
            logger.log('a log message');
            logger.warn("a warning to you");
            logger.error('an error condition');

            throw new Error('oops!  an error occurred.');
        }

        return foo;
    }

    a_method() {
        let foo = false;

        logger.debug('a debug message');

        if (foo) {
            logger.log('a log message');
            logger.warn("a warning to you");
            logger.error('an error condition');

            throw new Error('oops!  an error occurred.');
        } else {
            const str = "__PLACEHOLDER__";
            throw str;
        }

        return foo;
    }
}