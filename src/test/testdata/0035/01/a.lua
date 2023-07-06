-- example Lua program

function a(parameter)
    logger.log("Test message.");
    if (! parameter)
        error("An error.");
    end if
    logger.warn([[Multi-line string
        brackets=0]]);
    logger.warn([===[Multi-line [[
        string ]] brackets=3]===]);
end