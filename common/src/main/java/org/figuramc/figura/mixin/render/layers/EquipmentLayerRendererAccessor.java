package org.figuramc.figura.mixin.render.layers;

import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(EquipmentLayerRenderer.class)
public interface EquipmentLayerRendererAccessor {
    @Accessor("equipmentAssets")
    EquipmentAssetManager figura$getAssetsManager();

    @Accessor("layerTextureLookup")
    Function<EquipmentLayerRenderer.LayerTextureKey, Identifier> layerTextureLookup();

    @Accessor("trimTextureLookup")
    Function<EquipmentLayerRenderer.TrimTextureKey, net.minecraft.client.resources.palette.PalettedTextureManager.Handle> trimTextureLookup();

    @Invoker("getColorForLayer")
    static int figura$getColorForLayer(EquipmentClientInfo.Layer layer, int i) {
        throw new AssertionError();
    }
}
