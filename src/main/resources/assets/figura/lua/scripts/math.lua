-- math utils --

function math.clamp(val, min, max)
  return math.min(math.max(val, min), max)
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
  return math.fmod((b - a), 360)
end

function math.lerpAngle(a, b, t)
  return a + shortAngle(a, b) * t
end

function math.sign(x)
  return x == 0 and 0 or x > 0 and 1 or -1
end

vec = vectors.vec