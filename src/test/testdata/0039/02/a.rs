pub fn __handle_error(expected: &'static str, location: &'static str) -> str {

    log::error!("[E-1] first log");

    cfg_if! {
        if #[cfg(debug_assertions)] {
            panic!("[E-2] Exception one, debug only.")
        } else {
            _ = expected;
            panic!("[E-3] Exception two.")
        }
    }

    log::error!("[E-4] second log");
}