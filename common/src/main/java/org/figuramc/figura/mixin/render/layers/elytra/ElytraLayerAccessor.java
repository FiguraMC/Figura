package org.figuramc.figura.mixin.render.layers.elytra;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraLayer.class)
public interface ElytraLayerAccessor<T extends LivingEntity, M extends EntityModel<T>> {

    @Intrinsic
    @Accessor("WINGS_LOCATION")
    static ResourceLocation getWingsLocation() {
        throw new AssertionError();
    }
}
