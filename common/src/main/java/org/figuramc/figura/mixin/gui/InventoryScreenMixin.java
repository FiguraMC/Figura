package org.figuramc.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryScreen.class, priority = 999)
public class InventoryScreenMixin {

    @Inject(method = "renderEntityInInventory", at = @At("HEAD"), cancellable = true)
    private static void renderEntityInInventoryFollowsMouse(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        if (!Configs.FIGURA_INVENTORY.value || AvatarManager.panic)
            return;

        UIHelper.drawEntity(x, y, size, (float) Math.atan(mouseY / 40f) * 20f, (float) -Math.atan(mouseX / 40f) * 20f, entity, new PoseStack(), EntityRenderMode.MINECRAFT_GUI);
        ci.cancel();
    }
}
