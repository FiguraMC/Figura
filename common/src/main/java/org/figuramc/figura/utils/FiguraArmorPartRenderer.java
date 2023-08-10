package org.figuramc.figura.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.model.ParentType;

public interface FiguraArmorPartRenderer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    void renderArmorPart(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, A model, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem, ParentType parentType);
}

