package org.figuramc.figura.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import org.figuramc.figura.commands.fabric.FiguraCommandsFabric;
import org.figuramc.figura.utils.fabric.FiguraResourceListenerImpl;
import org.figuramc.figura.FiguraMod;

public class FiguraModFabric extends FiguraMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onClientInit();
        FiguraCommandsFabric.init();
        // we cast here to the impl that implements synchronus as the manager wants
        // register reload listener
        ResourceManagerHelper managerHelper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        getResourceListeners().forEach(figuraResourceListener -> managerHelper.registerReloadListener((FiguraResourceListenerImpl)figuraResourceListener));
    }
}
