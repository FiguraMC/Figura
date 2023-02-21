package org.moon.figura.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.moon.figura.resources.FiguraRuntimeResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {

    @ModifyVariable(at = @At(value = "HEAD"), method = "createReload", argsOnly = true)
    private List<PackResources> createReload(List<PackResources> packs) {
        List<PackResources> list = new ArrayList<>(packs);
        FiguraRuntimeResources.joinFuture();
        list.add(FiguraRuntimeResources.PACK);
        return list;
    }
}
