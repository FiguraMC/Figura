package org.figuramc.figura.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.model.ParentType;

public interface FiguraArmorPartRenderer<S extends HumanoidRenderState, A extends HumanoidModel<S>> {
    void renderArmorPart(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType);
}

