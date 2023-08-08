package org.figuramc.figura.font;

import com.google.gson.JsonObject;
import org.figuramc.figura.utils.JsonUtils;

import static org.figuramc.figura.font.EmojiContainer.*;

public class EmojiMetadata {
    public final int frames;
    public final int frameTime;
    public final int width;
    private int frameTimer;
    private int curFrame;

    public EmojiMetadata(int frames, int frameTime, int width) {
        this.frames = frames;
        this.frameTime = frameTime;
        this.width = width;
    }

    public EmojiMetadata(JsonObject entry) {
        this(entry.get(JSON_KEY_FRAMES).getAsInt(), entry.get(JSON_KEY_FRAME_TIME).getAsInt(), JsonUtils.getIntOrDefault(entry, JSON_KEY_WIDTH, 8));
    }

    public void tickAnimation() {
        frameTimer++;
        if (frameTimer >= frameTime) {
            frameTimer -= frameTime;
            curFrame = (curFrame + 1) % frames;
        }
    }

    public int getCurrentFrame() {
        return curFrame;
    }
}
