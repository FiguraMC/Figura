package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryScreen.class, priority = 999)
public class InventoryScreenMixin {

    @Inject(method = "renderEntityInInventory", at = @At("HEAD"), cancellable = true)
    private static void renderEntityInInventory(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
        if (!Config.FIGURA_INVENTORY.asBool() || AvatarManager.panic)
            return;

        UIHelper.drawEntity(x, y, size, mouseX, mouseY, entity, new PoseStack(), EntityRenderMode.MINECRAFT_GUI);
        ci.cancel();
    }
}
