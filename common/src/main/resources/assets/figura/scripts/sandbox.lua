-- sandbox --

_VERSION = "Lua 5.2 - Figura"

-- yeet FileIO and gc globals
debug = nil
dofile = nil
loadfile = nil
collectgarbage = nil

local _original_rep = string.rep
string.rep = function(s, n, sep)
    if n > 10000000 then
        error("string.rep: too many repetitions (" .. n .. " > 10000000), 3)
    end
    return _original_rep(s, n, sep)
end

-- GS easter egg
_GS = _G
