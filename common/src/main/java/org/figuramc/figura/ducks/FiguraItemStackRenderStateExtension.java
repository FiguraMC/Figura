package org.figuramc.figura.ducks;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FiguraItemStackRenderStateExtension {
    void figura$setItemStack(@Nullable ItemStack itemStack);
    ItemStack figura$getItemStack();
    boolean figura$isLeftHanded();
    ItemDisplayContext figura$getDisplayContext();
    ItemTransform figura$getItemTransform();
    List<BakedQuad> figura$getQuads();
}
