export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('[E-16]');
            console.warn("[E-17]");
            console.error("[E-18]");

            throw new Error("[E-19]");
        }

        return 'NULL';
    }
}