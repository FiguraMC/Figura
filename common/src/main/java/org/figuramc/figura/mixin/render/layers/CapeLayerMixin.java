package org.figuramc.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.PlayerModelAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Unique
    private Avatar avatar;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At("HEAD"))
    private void preRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, AbstractClientPlayer entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        ItemStack itemStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (entity.isInvisible() || itemStack.is(Items.ELYTRA))
            return;

        avatar = AvatarManager.getAvatar(entity);
        if (avatar == null)
            return;

        // Acquire reference to fake cloak
        ModelPart fakeCloak = ((PlayerModelAccessor) getParentModel()).figura$getFakeCloak();
        ModelPart realCloak = ((PlayerModelAccessor) getParentModel()).figura$getCloak();

        // Do math for fake cloak
        fakeCloak.copyFrom(realCloak);

        // REFERENCED FROM CODE IN CapeLayer (CapeFeatureRenderer for Yarn)
        double d = Mth.lerp(tickDelta, entity.xCloakO, entity.xCloak) - Mth.lerp(tickDelta, entity.xo, entity.getX());
        double e = Mth.lerp(tickDelta, entity.yCloakO, entity.yCloak) - Mth.lerp(tickDelta, entity.yo, entity.getY());
        double m = Mth.lerp(tickDelta, entity.zCloakO, entity.zCloak) - Mth.lerp(tickDelta, entity.zo, entity.getZ());
        float n = Mth.rotLerp(tickDelta, entity.yBodyRotO, entity.yBodyRot);
        n = (float) Math.toRadians(n);
        double o = Mth.sin(n);
        double p = -Mth.cos(n);
        float q = (float) e * 10f;
        q = Mth.clamp(q, -6f, 32f);
        float r = (float) (d * o + m * p) * 100f;
        r = Mth.clamp(r, 0f, 150f);
        float s = (float) (d * p - m * o) * 100f;
        s = Mth.clamp(s, -20f, 20f);
        r = Math.max(r, 0f);
        float t = Mth.lerp(tickDelta, entity.oBob, entity.bob);
        q += Mth.sin(Mth.lerp(tickDelta, entity.walkDistO, entity.walkDist) * 6f) * 32f * t;

        // Just going to ignore the fact that vanilla uses XZY rotation order for capes...
        // As a result, the cape rotation is slightly off.
        // Another inaccuracy results from the fact that the cape also moves its position without moving its pivot point,
        // I'm pretty sure. This is due to it using the matrix stack instead of setting x,y,z,xRot,yRot,zRot on the parts.
        // The cape functions completely differently than all other model parts of the player. Quite frankly,
        // I don't want to deal with it any more than I already have, and I'm just going to leave this alone now and call it
        // close enough.

        // If someone wants to spend the time to correct these inaccuracies for us, feel free to make a pull request.

        // pos
        if (itemStack.isEmpty() || (avatar.luaRuntime != null && !avatar.luaRuntime.vanilla_model.CHESTPLATE_BODY.checkVisible())) {
            if (entity.isCrouching()) {
                q += 25f;
                fakeCloak.y = 2.25f;
                fakeCloak.z = -0.25f;
            } else {
                fakeCloak.y = 0f;
                fakeCloak.z = 0f;
            }
        } else if (entity.isCrouching()) {
            q += 25f;
            fakeCloak.y = 0.85f;
            fakeCloak.z = 0.15f;
        } else {
            fakeCloak.y = -1f;
            fakeCloak.z = 1f;
        }

        // rot
        fakeCloak.setRotation(
                (float) Math.toRadians(6f + r / 2f + q),
                (float) -Math.toRadians(s / 2f),
                (float) Math.toRadians(s / 2f)
        );

        // Copy rotations from fake cloak
        if (avatar.luaRuntime != null) {
            VanillaPart part = avatar.luaRuntime.vanilla_model.CAPE;
            EntityModel<?> model = getParentModel();
            part.save(model);
            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
                part.preTransform(model);
        }

        avatar.capeRender(entity, multiBufferSource, poseStack, light, tickDelta, fakeCloak);

        // Setup visibility for real cloak
        if (RenderUtils.vanillaModelAndScript(avatar))
            avatar.luaRuntime.vanilla_model.CAPE.posTransform(getParentModel());
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At("RETURN"))
    private void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (avatar == null)
            return;

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.CAPE.restore(getParentModel());

        avatar = null;
    }
}
