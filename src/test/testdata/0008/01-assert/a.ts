export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('[E-1] a log line');
            console.warn("[E-2] a warning to you");
            console.error("[E-3] an error message");

            throw new Error("[E-4] this won't occur");
        }

        return 'NULL';
    }
}