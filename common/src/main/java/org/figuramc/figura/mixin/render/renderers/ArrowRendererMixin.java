package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraProjectileRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowRenderer.class)
public abstract class ArrowRendererMixin<T extends AbstractArrow, S extends ArrowRenderState> extends EntityRenderer<T, S> {

    protected ArrowRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", ordinal = 0), method = "submit(Lnet/minecraft/client/renderer/entity/state/ArrowRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V")
    private Model<S> render(Model<S> par1, @Local(argsOnly = true) S arrowRenderState) {
        Integer id = ((FiguraEntityRenderStateExtension)arrowRenderState).figura$getEntityId();
        if (id == null)
            return par1;

        Projectile arrow = (Projectile) AvatarManager.ENTITY_CACHE.computeIfAbsent(id, (id2) -> Minecraft.getInstance().level.getEntity(id2));
        if (arrow == null)
            return par1;

        float tickDelta = ((FiguraProjectileRenderStateExtension)arrowRenderState).figura$getTickDelta();
        Entity owner = arrow.getOwner();

        Avatar avatar = AvatarManager.getAvatar(owner);
        if (avatar == null || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 0)
            return par1;

        ((FiguraSubmitCallBackExtension)par1).figura$addPreRenderingCallback((multiBufferSource, poseStack) -> {
            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(avatar);
            FiguraMod.pushProfiler("arrowRender");

            FiguraMod.pushProfiler("event");
            boolean bool = avatar.arrowRenderEvent(tickDelta, EntityAPI.wrap(arrow));

            FiguraMod.popPushProfiler("render");
            if (bool || avatar.renderArrow(poseStack, multiBufferSource, tickDelta, arrowRenderState.lightCoords)) {
                poseStack.popPose();
                // this will skip the original render call
                return false;
            }

            FiguraMod.popProfiler(4);
            return true;
        } );
        return par1;

    }

    @Inject(at = @At("HEAD"), method = "extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;Lnet/minecraft/client/renderer/entity/state/ArrowRenderState;F)V")
    void appendFiguraProperties(T abstractArrow, S arrowRenderState, float f, CallbackInfo ci) {
        ((FiguraProjectileRenderStateExtension)arrowRenderState).figura$setTickDelta(f);
    }
}
