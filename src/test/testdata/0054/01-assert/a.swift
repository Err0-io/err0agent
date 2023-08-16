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
}