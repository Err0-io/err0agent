export class A {
    public method(a:number):string {
        const foo:boolean = false;

        console.debug("A.method(" + a + ")");

        if (foo) {
            console.log('a log line');
            console.warn("a warning to you");
            console.error("an error message");

            const not:string = 'Not a __PLACEHOLDER__';
            const str1:string = `__PLACEHOLDER__`;
            const str2:string = '__PLACEHOLDER__';
            const str3:string = "__PLACEHOLDER__";

            throw new Error("this won't occur");
        }

        const obj = {
            "key": "__PLACEHOLDER__",
            "value": 1,
        };

        return 'NULL';
    }
}