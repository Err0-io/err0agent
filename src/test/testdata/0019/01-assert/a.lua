-- example Lua program

function a(parameter)
    logger.log("[E-1] Test message.");
    if (! parameter)
        error("[E-2] An error.");
    end if
    logger.warn([[[E-3] Multi-line string
        brackets=0]]);
    logger.warn([===[[E-4] Multi-line [[
        string ]] brackets=3]===]);
end

--[[
Multi line comment
A second line
--]]

function b(parameter) {
    logger.log("[E-5] Test message.");
}