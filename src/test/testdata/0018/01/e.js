// a closure
(function(){
    let foo = false;

    console.debug('a debug message');

    if (foo) {
        console.log('');
        console.warn("");
        console.error('');
    }

    throw new Error('');
});