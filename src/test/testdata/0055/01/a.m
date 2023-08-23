void c_method() {
    NSLog(@"Objective-C style log");
}

@implementation A

- (void)method {
    NSLog(@"An example log");
    os_log(@"An example log");
    [NSException raise:@"An example exception"];
    c_method();
}

+ (void)classMethod {
    NSLog(@"A log, from a class method.");
}

@end

void cpp_method() {
    throw std::exception("A c++ style exception");
}