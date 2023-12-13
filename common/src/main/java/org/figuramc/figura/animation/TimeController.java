package org.figuramc.figura.animation;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.Level;
import org.figuramc.figura.mixin.MinecraftAccesor;

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

    // This one is tricky, we get the tick target miliseconds and use the value vanilla's timer has on msPerTick which is 50, then divide by the original value it used to get that (1000/20), so 20
    public float getDiff() {
        return Minecraft.getInstance().isPaused() ? 0 : ((time - lastTime) / ((MinecraftAccesor)Minecraft.getInstance()).figura$invokeGetTickTargetMillis(50.0f))/ 20.0f;
    }
}
