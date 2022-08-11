package org.moon.figura.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ui.UIHelper;

public class PaperDoll {

    private static Long lastActivityTime = 0L;

    public static void render(PoseStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (!(boolean) Config.HAS_PAPERDOLL.value ||
                player == null ||
                !Minecraft.renderNames() ||
                minecraft.options.renderDebug ||
                ((boolean) Config.FIRST_PERSON_PAPERDOLL.value && !minecraft.options.getCameraType().isFirstPerson()) ||
                player.isSleeping())
            return;

        //check if should stay always on
        if (!(boolean) Config.PAPERDOLL_ALWAYS_ON.value) {
            //if action - reset activity time and enable can draw
            if (player.isSprinting() ||
                    player.isCrouching() ||
                    player.isAutoSpinAttack() ||
                    player.isVisuallySwimming() ||
                    player.isFallFlying() ||
                    player.isBlocking() ||
                    player.onClimbable() ||
                    player.getAbilities().flying)
                lastActivityTime = System.currentTimeMillis();

            //if activity time is greater than duration - return
            else if(System.currentTimeMillis() - lastActivityTime > 1000L)
                return;
        }

        //draw
        float screenWidth = Minecraft.getInstance().getWindow().getWidth();
        float screenHeight = Minecraft.getInstance().getWindow().getHeight();
        float guiScale = (float) Minecraft.getInstance().getWindow().getGuiScale();

        float scale = (float) Config.PAPERDOLL_SCALE.value;
        float x = scale * 25f;
        float y = scale * 45f;
        x += ((float) Config.PAPERDOLL_X.value / 100f) * screenWidth / guiScale;
        y += ((float) Config.PAPERDOLL_Y.value / 100f) * screenHeight / guiScale;

        UIHelper.drawEntity(
                x, y,
                scale * 30f,
                (float) Config.PAPERDOLL_PITCH.value, (float) Config.PAPERDOLL_YAW.value,
                player, stack, true
        );
    }
}
