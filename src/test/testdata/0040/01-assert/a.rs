pub fn __handle_error(expected: &'static str, location: &'static str) -> str {

    log::error!("first log");

    cfg_if! {
        if #[cfg(debug_assertions)] {
            panic!("[E-1] Exception one, debug only.")
        } else {
            _ = expected;
            panic!("[E-2] Exception two.")
        }
    }

    log::error!("second log");
}