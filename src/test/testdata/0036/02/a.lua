-- example Lua program

function a(parameter)
    logger.log("[E-1] Test message.");
    if (! parameter)
        error("An error.");
    end if
    logger.warn([[[E-2] Multi-line string
        brackets=0]]);
    logger.warn([===[[E-3] Multi-line [[
        string ]] brackets=3]===]);
end