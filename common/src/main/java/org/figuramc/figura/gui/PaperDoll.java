package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Vector3f;

public class PaperDoll {

    private static Long lastActivityTime = 0L;

    public static void render(GuiGraphics gui, boolean force) {
        Minecraft minecraft = Minecraft.getInstance();
        LivingEntity entity = minecraft.getCameraEntity() instanceof LivingEntity e ? e : null;
        Avatar avatar;

        if ((!Configs.HAS_PAPERDOLL.value && !force) ||
                entity == null ||
                !Minecraft.renderNames() ||
                minecraft.getDebugOverlay().showDebugScreen() ||
                (Configs.FIRST_PERSON_PAPERDOLL.value && !minecraft.options.getCameraType().isFirstPerson() && !force) ||
                entity.isSleeping())
            return;

        // check if it should stay always on
        if (!Configs.PAPERDOLL_ALWAYS_ON.value && !force && (avatar = AvatarManager.getAvatar(entity)) != null && avatar.luaRuntime != null && !avatar.luaRuntime.renderer.forcePaperdoll) {
            // if action - reset activity time and enable can draw
            if (entity.isSprinting() ||
                    entity.isCrouching() ||
                    entity.isAutoSpinAttack() ||
                    entity.isVisuallySwimming() ||
                    entity.isFallFlying() ||
                    entity.isBlocking() ||
                    entity.onClimbable() ||
                    (entity instanceof Player p && p.getAbilities().flying))
                lastActivityTime = System.currentTimeMillis();

            // if activity time is greater than duration - return
            else if (System.currentTimeMillis() - lastActivityTime > 1000L)
                return;
        }

        // draw
        Window window = minecraft.getWindow();
        float screenWidth = window.getWidth();
        float screenHeight = window.getHeight();
        float guiScale = (float) window.getGuiScale();

        float scale = Configs.PAPERDOLL_SCALE.tempValue;
        float x = scale * 25f;
        float y = scale * 45f;
        x += (Configs.PAPERDOLL_X.tempValue / 100f) * screenWidth / guiScale;
        y += (Configs.PAPERDOLL_Y.tempValue / 100f) * screenHeight / guiScale;

        UIHelper.drawEntity(
                x, y,
                scale * 30f,
                Configs.PAPERDOLL_PITCH.tempValue, Configs.PAPERDOLL_YAW.tempValue,
                entity, gui, new Vector3f(), EntityRenderMode.PAPERDOLL
        );
    }
}
