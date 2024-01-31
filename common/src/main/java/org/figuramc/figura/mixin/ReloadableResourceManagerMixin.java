package org.figuramc.figura.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import org.figuramc.figura.resources.FiguraRuntimeResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SimpleReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {

    @ModifyVariable(at = @At(value = "HEAD"), method = "createFullReload", argsOnly = true)
    private List<PackResources> createReload(List<PackResources> packs) {
        List<PackResources> list = new ArrayList<>(packs);

        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i).getName();
            if ("Fabric Mods".equals(id) || "vanilla".equals(id))
                index = i + 1;
        }

        FiguraRuntimeResources.joinFuture();
        list.add(index, FiguraRuntimeResources.PACK);

        return list;
    }
}
