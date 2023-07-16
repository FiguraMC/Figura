package org.moon.figura.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.fabric.FiguraResourceListenerImpl;

import java.util.List;

public class FiguraModFabric extends FiguraMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onClientInit();

        //we cast here to the impl that implements synchronus
        //register reload listener
        ResourceManagerHelper managerHelper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        getResourceListeners().forEach(figuraResourceListener -> managerHelper.registerReloadListener((FiguraResourceListenerImpl)figuraResourceListener));
    }
}
