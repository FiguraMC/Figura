-- sandbox --
--set up garbage collector to not be trash
--collectgarbage("setpause", 0)

-- yeet FileIO and gc globals
dofile = nil
loadfile = nil
collectgarbage = nil

-- GS easter egg
_GS = _G