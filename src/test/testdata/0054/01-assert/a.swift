/*

    an example swift file.

 */

/*
    /* a nested, multi-line comment. */
 */

enum AnErrorType : Error {
    case noErrorNumber
    case withErrorNumber(String)
}

public struct Structure {
    public var example: String

    public func staticMessage<T>(_ object: T) -> Never where T : String {
        fatalError("[E-1] This error is fatal.")
    }

    public func dynamicMessage<T>(_ object: T) -> Never where T : String {
        fatalError("[E-2] This fatal error is dynamic \(object)")
    }

    public func notADynamicMessage<T>(_ object: T) -> Never where T : String {
        fatalError(#"[E-3] This fatal error is static \(object)"#)
    }

    public func dynamicMultiLineMessage<T>(_ object: T) -> Never where T : String {
        fatalError("""
[E-4] This fatal error is dynamic \(object)
""")
    }

    public func staticMultiLineMessage<T>(_ object: T) -> Never where T : String {
        fatalError(#"""
[E-5] This fatal error is static \(object)
"""#)
    }
}

public func aFunctionThatLogs() -> Void {
    let logger = Logger();

    logger.log("[E-6] default level")
    logger.trace("trace level")
    logger.debug("debug level")
    logger.notice("[E-7] notice level")
    logger.info("[E-8] info level")
    logger.error("[E-9] error level")
    logger.warning("[E-10] warning level")
    logger.fault("[E-11] fault level")
    logger.critical("[E-12] critical level")
}

public func aFunctionThatThrows() throws -> Never {
    throw AnErrorType.noErrorNumber
    throw AnErrorType.withErrorNumber("[E-13] An error message")
}