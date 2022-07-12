package org.moon.figura.animation;

import net.minecraft.Util;

public class TimeController {

    private long startTime = 0L;
    private long lastTime = 0L;
    private long pauseTime = 0L;

    public void init(float offsetSeconds) {
        this.init((long) (offsetSeconds * 1000L));
    }

    public void init(long offset) {
        startTime = Util.getMillis() + offset;
        lastTime = startTime;
    }

    public void tick() {
        lastTime = Util.getMillis();
    }

    public void reset() {
        startTime = 0L;
        lastTime = 0L;
    }

    public void pause() {
        pauseTime = Util.getMillis();
    }

    public void resume() {
        startTime += Util.getMillis() - pauseTime;
    }

    public long getElapsedTime() {
        return lastTime - startTime;
    }

    public float getElapsedTimeSeconds() {
        return getElapsedTime() / 1000f;
    }
}
