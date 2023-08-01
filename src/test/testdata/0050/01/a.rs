pub fn __handle_error(expected: &'static str, location: &'static str) -> str {

    log::error!("first log");

    cfg_if! {
        if #[cfg(debug_assertions)] {
            panic!("Exception one, debug only.")
        } else {
            _ = expected;
            panic!("Exception two.")
        }
    }

    print("__PLACEHOLDER__");
    print("Not a __PLACEHOLDER__");

    log::error!("second log");
}