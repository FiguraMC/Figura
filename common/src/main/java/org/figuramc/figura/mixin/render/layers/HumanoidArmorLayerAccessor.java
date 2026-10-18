package org.figuramc.figura.mixin.render.layers;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {
    @Intrinsic
    @Invoker("usesInnerModel")
    boolean figura$usesInnerModel(EquipmentSlot armorSlot);
}
