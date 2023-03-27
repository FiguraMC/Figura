package org.moon.figura.entries;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.widgets.PanelSelectorWidget;
import org.moon.figura.lua.FiguraAPIManager;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.permissions.PermissionManager;

import java.util.HashSet;
import java.util.Set;

public class EntryPointManager {

    public static void init() {
        //APIs
        var apis = load("figura_api", FiguraAPI.class);
        FiguraAPIManager.initEntryPoints(apis);
        FiguraDocsManager.initEntryPoints(apis);

        //other
        PermissionManager.initEntryPoints(load("figura_permissions", FiguraPermissions.class));
        PanelSelectorWidget.initEntryPoints(load("figura_screen", FiguraScreen.class));
        VanillaModelAPI.initEntryPoints(load("figura_vanilla_part", FiguraVanillaPart.class));
    }

    private static <T> Set<T> load(String name, Class<T> clazz) {
        Set<T> ret = new HashSet<>();

        for (EntrypointContainer<T> entrypoint : FabricLoader.getInstance().getEntrypointContainers(name, clazz)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            String modId = metadata.getId();
            try {
                ret.add(entrypoint.getEntrypoint());
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load entrypoint of mod {}", modId, e);
            }
        }

        return ret;
    }
}
