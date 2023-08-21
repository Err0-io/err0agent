- (void)method {
    NSLog(@"[E-1] An example log");
    os_log(@"[E-2] An example log");
    [NSException raise:@"[E-3] An example exception"];
}