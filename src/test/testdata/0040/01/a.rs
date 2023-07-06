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

    log::error!("second log");
}