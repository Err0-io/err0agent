-- example Lua program

function b(text)
    logger.log(text);
end

function a(parameter)
    logger.log("Test message.");
    if (! parameter)
        error("An error.");
    end if
    logger.warn([[Multi-line string
        brackets=0]]);
    logger.warn([===[Multi-line [[
        string ]] brackets=3]===]);
    b("__PLACEHOLDER__")
    b("Not a __PLACEHOLDER__")
    b('__PLACEHOLDER__')
    -- not a placeholder:
    b([===[__PLACEHOLDER__]===])
end