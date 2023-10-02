-- math utils --

function math.clamp(val, min, max)
   return min > max and max or val > max and max or val < min and min or val
end

function math.lerp(a, b, t)
  return a + (b - a) * t
end

function math.round(arg)
  return math.floor(arg + 0.5)
end

function math.map(value, min1, max1, min2, max2)
  return (value - min1) / (max1 - min1) * (max2 - min2) + min2
end

function math.shortAngle(a, b)
    local x = (a - b) % 360
    return x - ((2 * x) % 360)
end

function math.lerpAngle(a, b, t)
  return (a + math.shortAngle(a, b) * t) % 360
end

function math.sign(x)
  return x == 0 and 0 or x > 0 and 1 or -1
end

vec = vectors.vec
math.playerScale = 0.9375
math.worldScale = 1.0666666667