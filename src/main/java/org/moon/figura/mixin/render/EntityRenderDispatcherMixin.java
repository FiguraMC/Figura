package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.LevelReader;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Unique private Avatar avatar;

    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void renderFlame(PoseStack stack, MultiBufferSource multiBufferSource, Entity entity, CallbackInfo ci) {
        avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaRuntime == null || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 0)
            return;

        if (!avatar.luaRuntime.renderer.renderFire) {
            avatar = null;
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderFlame", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private float renderFlameRot(float f) {
        return UIHelper.paperdoll ? UIHelper.fireRot : f;
    }

    @ModifyVariable(method = "renderFlame", at = @At("STORE"), ordinal = 0)
    private TextureAtlasSprite firstFireTexture(TextureAtlasSprite sprite) {
        if (avatar == null)
            return sprite;

        ResourceLocation layer = avatar.luaRuntime.renderer.fireLayer1;
        return layer != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer) : sprite;
    }

    @ModifyVariable(method = "renderFlame", at = @At("STORE"), ordinal = 1)
    private TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite) {
        if (avatar == null)
            return sprite;

        ResourceLocation layer1 = avatar.luaRuntime.renderer.fireLayer1;
        ResourceLocation layer2 = avatar.luaRuntime.renderer.fireLayer2;
        avatar = null;

        return layer2 != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer2) : layer1 != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer1) : sprite;
    }

    @ModifyVariable(method = "renderShadow", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static float modifyShadowSize(float h, PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, LevelReader levelReader) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null && avatar.luaRuntime != null && avatar.luaRuntime.renderer.shadowRadius != null && avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
            return avatar.luaRuntime.renderer.shadowRadius;
        return h;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void render(E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        Entity owner = entity.getFirstPassenger();
        if (owner == null)
            return;

        Avatar avatar = AvatarManager.getAvatar(owner);
        if (avatar == null || avatar.luaRuntime == null || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 0)
            return;

        if (!avatar.luaRuntime.renderer.renderVehicle)
            ci.cancel();
    }
}
