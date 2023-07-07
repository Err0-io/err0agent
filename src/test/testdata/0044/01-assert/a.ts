export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('[E-1] a log line');
            console.warn("[E-2] a warning to you");
            console.error("[E-3] an error message");

            const not:string = 'Not a __PLACEHOLDER__';
            const str1:string = `E-4`;
            const str2:string = 'E-5';
            const str3:string = "E-6";

            throw new Error("[E-7] this won't occur");
        }

        const obj = {
            "key": "E-8",
            "value": 1,
        };

        return 'NULL';
    }
}