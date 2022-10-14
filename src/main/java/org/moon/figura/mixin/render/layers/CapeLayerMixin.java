package org.moon.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.ducks.PlayerModelAccessor;
import org.moon.figura.trust.TrustContainer;
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
    public void preRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        avatar = AvatarManager.getAvatar(abstractClientPlayer);
        if (avatar == null || avatar.luaRuntime == null)
            return;

        //Acquire reference to fake cloak
        ModelPart fakeCloak = ((PlayerModelAccessor) getParentModel()).figura$getFakeCloak();
        ModelPart realCloak = ((PlayerModelAccessor) getParentModel()).figura$getCloak();

        //Do math for fake cloak
        fakeCloak.x = realCloak.x;
        fakeCloak.y = realCloak.y;
        fakeCloak.z = realCloak.z;

        //REFERENCED FROM CODE IN CapeLayer (CapeFeatureRenderer for Yarn)
        double d = Mth.lerp(h, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak) - Mth.lerp(h, abstractClientPlayer.xo, abstractClientPlayer.getX());
        double e = Mth.lerp(h, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak) - Mth.lerp(h, abstractClientPlayer.yo, abstractClientPlayer.getY());
        double m = Mth.lerp(h, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak) - Mth.lerp(h, abstractClientPlayer.zo, abstractClientPlayer.getZ());
        //Change n to use lerp, to "fix" https://bugs.mojang.com/browse/MC-127749
        //float n = abstractClientPlayer.yBodyRotO + (abstractClientPlayer.yBodyRot - abstractClientPlayer.yBodyRotO);
        float n = Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
        double o = Mth.sin(n * ((float)Math.PI / 180));
        double p = -Mth.cos(n * ((float)Math.PI / 180));
        float q = (float)e * 10.0f;
        q = Mth.clamp(q, -6.0f, 32.0f);
        float r = (float)(d * o + m * p) * 100.0f;
        r = Mth.clamp(r, 0.0f, 150.0f);
        float s = (float)(d * p - m * o) * 100.0f;
        s = Mth.clamp(s, -20.0f, 20.0f);
        if (r < 0.0f) {
            r = 0.0f;
        }
        float t = Mth.lerp(h, abstractClientPlayer.oBob, abstractClientPlayer.bob);
        q += Mth.sin(Mth.lerp(h, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist) * 6.0f) * 32.0f * t;
        if (abstractClientPlayer.isCrouching()) {
            q += 25.0f;
            fakeCloak.y += 0.5f;
            fakeCloak.z -= 2;
        }

        //Just going to ignore the fact that vanilla uses XZY rotation order for capes...
        //As a result, the cape rotation is slightly off.
        //Another inaccuracy results from the fact that the cape also moves its position without moving its pivot point,
        //I'm pretty sure. This is due to it using the matrix stack instead of setting x,y,z,xRot,yRot,zRot on the parts.
        //The cape functions completely differently than all other model parts of the player. Quite frankly,
        //I don't want to deal with it any more than I already have, and I'm just going to leave this alone now and call it
        //close enough.

        //If someone wants to spend the time to correct these inaccuracies for us, feel free to make a pull request.
        fakeCloak.xRot = (float) Math.toRadians(6f + r / 2f + q);
        fakeCloak.yRot = (float) -Math.toRadians(s / 2f);
        fakeCloak.zRot = (float) Math.toRadians(s / 2f);

        //Copy rotations from fake cloak
        avatar.luaRuntime.vanilla_model.CAPE.store(getParentModel());

        //Setup visibility for real cloak
        if (avatar.trust.get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1)
            avatar.luaRuntime.vanilla_model.CAPE.alter(getParentModel());
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At("TAIL"))
    public void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (avatar == null)
            return;

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.CAPE.restore(getParentModel());

        avatar = null;
    }
}
