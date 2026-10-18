package org.figuramc.figura.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {
    @WrapOperation(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/extensions/common/IClientItemExtensions;getGenericArmorModel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/client/model/Model;)Lnet/minecraft/client/model/Model;"))
    private Model<?> figura$captureCustomModel(IClientItemExtensions extensions, ItemStack item,
            EquipmentClientInfo.LayerType layer, Model<?> model, Operation<Model<?>> original,
            @Share("figura$customModelPrepared") LocalBooleanRef prepared) {
        Model<?> customModel = original.call(extensions, item, layer, model);
        prepared.set(customModel != model && FiguraFeatureRenderer.isModelPrepared(model));
        return customModel;
    }

    @WrapOperation(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"))
    private <S> void figura$submitCustomModel(OrderedSubmitNodeCollector collector, Model<? super S> model,
            S state, PoseStack pose, RenderType type, int light, int overlay, int color, UvMapping uv,
            int outline, Operation<Void> original,
            @Share("figura$customModelPrepared") LocalBooleanRef prepared) {
        // NeoForge may replace the model after Figura has applied its armor transforms.
        if (prepared.get())
            original.call(collector, FiguraFeatureRenderer.snapshotModel(model), Unit.INSTANCE,
                    pose, type, light, overlay, color, uv, outline);
        else
            original.call(collector, model, state, pose, type, light, overlay, color, uv, outline);
    }
}
