function _l(aWarningToYou: string) {
    console.log(aWarningToYou);
}

export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('a log line');
            _l("[E-1] a warning to you");
            console.error("an error message");
            console.error("[E-2] ERROR! an error message");

            throw new Error("[E-3] this won't occur");
        }

        return 'NULL';
    }
}