// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('[E-12]');
        console.warn("[E-13]");
        console.error('[E-14]');
    }

    throw new Error('[E-15]');
});