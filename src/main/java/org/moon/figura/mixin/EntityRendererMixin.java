package org.moon.figura.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    private static Avatar avatar = null;

    @Inject(at = @At("HEAD"), method = "render")
    private void testDrawing(Entity e, float yaw, float delta, MatrixStack m, VertexConsumerProvider vcp, int l, CallbackInfo ci) {

        //Create avatar if not done already
        if (avatar == null) {
            LocalAvatarFetcher.load();
            if (!LocalAvatarFetcher.ALL_AVATARS.isEmpty()) {
                NbtCompound nbt = LocalAvatarLoader.loadAvatar(LocalAvatarFetcher.ALL_AVATARS.get(0).getPath());
                if (nbt != null) {
                    avatar = new Avatar(nbt);
                    LocalAvatarLoader.saveNbt();
                    FiguraMod.LOGGER.warn(avatar.toString());
                }
            }
        }

        //Render avatar with params
        avatar.renderer.entity = e;
        avatar.renderer.yaw = yaw;
        avatar.renderer.tickDelta = delta;
        avatar.renderer.matrices = m;
        avatar.renderer.light = l;
        avatar.renderer.vcp = vcp;

        avatar.renderer.render();
    }

}
