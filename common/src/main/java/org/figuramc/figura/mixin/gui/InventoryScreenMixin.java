package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryScreen.class, priority = 999)
public class InventoryScreenMixin {

    @Inject(method = "renderEntityInInventoryFollowsMouse", at = @At("HEAD"), cancellable = true)
    private static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int x, int y, int i, int j, int size, float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        if (!Configs.FIGURA_INVENTORY.value || AvatarManager.panic)
            return;
        float g = (float)(x + i) / 2.0F;
        float h = (float)(y + j) / 2.0F;
        float pitch = g - mouseX;
        float yaw = h - mouseY;
        Vector3f modelOffset = new Vector3f(0.0F, -(entity.getBbHeight() / 2.0F + yOffset), 0);

        // Scissor is disabled here as enabling it would cut off nameplate rendering and taller models.
        // guiGraphics.enableScissor(x, y, i, j);
        UIHelper.drawEntity(g, h, size, (float) Math.atan(yaw / 40f) * 20f, (float) -Math.atan(pitch / 40f) * 20f, entity, guiGraphics, modelOffset, EntityRenderMode.MINECRAFT_GUI);
        // guiGraphics.disableScissor();
        ci.cancel();
    }
}
