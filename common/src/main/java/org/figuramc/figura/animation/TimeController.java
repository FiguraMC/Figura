package org.figuramc.figura.animation;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

public class TimeController {

    private long lastTime, time, pauseTime;

    public void init() {
        pauseTime = 0L;
        lastTime = time = Util.getMillis();
    }

    public void tick() {
        lastTime = time;
        time = Util.getMillis();
    }

    public void reset() {
        lastTime = time = pauseTime = 0L;
    }

    public void pause() {
        lastTime = time;
        pauseTime = Util.getMillis();
    }

    public void resume() {
        long diff = Util.getMillis() - pauseTime;
        lastTime += diff;
        time += diff;
    }

    public float getDiff() {
        return Minecraft.getInstance().isPaused() ? 0 : (time - lastTime) / 1000f;
    }
}
