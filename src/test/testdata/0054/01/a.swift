/*

    an example swift file.

 */

/*
    /* a nested, multi-line comment. */
 */

public struct Structure {
    public var example: String

    public func staticMessage<T>(_ object: T) -> Never where T : String {
        fatalError("This error is fatal.")
    }

    public func dynamicMessage<T>(_ object: T) -> Never where T : String {
        fatalError("This fatal error is dynamic \(object)")
    }

    public func notADynamicMessage<T>(_ object: T) -> Never where T : String {
        fatalError(#"This fatal error is static \(object)"#)
    }

    public func dynamicMultiLineMessage<T>(_ object: T) -> Never where T : String {
        fatalError("""
This fatal error is dynamic \(object)
""")
    }

    public func staticMultiLineMessage<T>(_ object: T) -> Never where T : String {
        fatalError(#"""
This fatal error is static \(object)
"""#)
    }
}

public func aFunctionThatLogs() -> Void {
    let logger = Logger();

    logger.log("default level")
    logger.trace("trace level")
    logger.debug("debug level")
    logger.notice("notice level")
    logger.info("info level")
    logger.error("error level")
    logger.warning("warning level")
    logger.fault("fault level")
    logger.critical("critical level")
}