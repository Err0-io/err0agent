function _l(aWarningToYou: string) {
    console.log(aWarningToYou);
}

export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('a log line');
            _l("a warning to you");
            console.error("an error message");
            console.error("ERROR! an error message");

            throw new Error("this won't occur");
        }

        return 'NULL';
    }
}