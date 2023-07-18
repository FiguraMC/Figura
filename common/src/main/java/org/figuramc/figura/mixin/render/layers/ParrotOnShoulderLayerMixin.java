package org.figuramc.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/render/layers/ParrotOnShoulderLayerMixin.java
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.model.ParentType;
import org.moon.figura.utils.RenderUtils;
=======
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/render/layers/ParrotOnShoulderLayerMixin.java
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParrotOnShoulderLayer.class)
public abstract class ParrotOnShoulderLayerMixin<T extends Player> extends RenderLayer<T, PlayerModel<T>> {

    public ParrotOnShoulderLayerMixin(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Shadow @Final private ParrotModel model;

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFZ)V", cancellable = true)
    private void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(player);
        if (!RenderUtils.vanillaModel(avatar))
            return;

        //script hide
        if (avatar.luaRuntime != null &&
                (leftShoulder && !avatar.luaRuntime.vanilla_model.LEFT_PARROT.checkVisible() ||
                !leftShoulder && !avatar.luaRuntime.vanilla_model.RIGHT_PARROT.checkVisible())
        ) {
            ci.cancel();
            return;
        }

        //pivot part
        CompoundTag compoundTag = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        EntityType.byString(compoundTag.getString("id")).filter((type) -> type == EntityType.PARROT).ifPresent((type) -> {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundTag.getInt("Variant")]));
            if (avatar.pivotPartRender(leftShoulder ? ParentType.LeftParrotPivot : ParentType.RightParrotPivot, stack -> {
                stack.translate(0d, 24d, 0d);
                float s = 16f;
                stack.scale(s, s, s);
                stack.mulPose(Vector3f.XP.rotationDegrees(180f));
                stack.mulPose(Vector3f.YP.rotationDegrees(180f));
                this.model.renderOnShoulder(stack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, limbAngle, limbDistance, headYaw, headPitch, player.tickCount);
            })) {
                ci.cancel();
            }
        });
    }
}
