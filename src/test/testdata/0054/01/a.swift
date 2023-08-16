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
}