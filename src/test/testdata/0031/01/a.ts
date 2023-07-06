export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('a log line');
            console.warn("a warning to you");
            console.error("an error message");

            throw new Error("this won't occur");
        }

        return 'NULL';
    }
}