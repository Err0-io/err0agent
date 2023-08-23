void c_method() {
    NSLog(@"[E-1] Objective-C style log");
}

@implementation A

- (void)method {
    NSLog(@"[E-2] An example log");
    os_log(@"[E-3] An example log");
    [NSException raise:@"[E-4] An example exception"];
    c_method();
}

+ (void)classMethod {
    NSLog(@"[E-5] A log, from a class method.");
}

@end