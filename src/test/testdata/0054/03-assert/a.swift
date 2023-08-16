/*

    an example swift file.

 */

/*
    /* a nested, multi-line comment. */
 */

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