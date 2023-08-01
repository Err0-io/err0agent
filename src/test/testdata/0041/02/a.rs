pub fn __handle_error(expected: &'static str, location: &'static str) -> str {

    log::error!("[E-1] first log");

    cfg_if! {
        if #[cfg(debug_assertions)] {
            panic!("Exception one, debug only.")
        } else {
            _ = expected;
            panic!("Exception two.")
        }
    }

    log::error!("[E-2] second log");
}