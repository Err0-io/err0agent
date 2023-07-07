-- example Lua program

function b(text)
    logger.log(text);
end

function a(parameter)
    logger.log("[E-1] Test message.");
    if (! parameter)
        error("[E-2] An error.");
    end if
    logger.warn([[[E-3] Multi-line string
        brackets=0]]);
    logger.warn([===[[E-4] Multi-line [[
        string ]] brackets=3]===]);
    b("E-5")
    b("Not a __PLACEHOLDER__")
    b('E-6')
    -- not a placeholder:
    b([===[__PLACEHOLDER__]===])
end