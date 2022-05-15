-- yeet FileIO globals
dofile = nil
loadfile = nil
collectgarbage = nil

-- GS easter egg
_GS = _G

-- math utils
function math.clamp(val, min, max)
    return math.min(math.max(val, min), max)
end

function math.lerp(a, b, t)
    return a + (b - a) * t
end

function math.round(arg)
    return math.floor(arg + 0.5)
end