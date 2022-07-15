export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('');
            console.warn("");
            console.error("");

            throw new Error("");
        }

        return 'NULL';
    }
}