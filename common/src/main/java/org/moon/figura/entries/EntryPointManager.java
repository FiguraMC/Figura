package org.moon.figura.entries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.moon.figura.gui.widgets.PanelSelectorWidget;
import org.moon.figura.lua.FiguraAPIManager;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.permissions.PermissionManager;

import java.util.Set;

public class EntryPointManager {

    public static void init() {
        //APIs
        Set<FiguraAPI> apis = load("figura_api", FiguraAPI.class);
        FiguraAPIManager.initEntryPoints(apis);
        FiguraDocsManager.initEntryPoints(apis);

        //other
        PermissionManager.initEntryPoints(load("figura_permissions", FiguraPermissions.class));
        PanelSelectorWidget.initEntryPoints(load("figura_screen", FiguraScreen.class));
        VanillaModelAPI.initEntryPoints(load("figura_vanilla_part", FiguraVanillaPart.class));
    }

    @ExpectPlatform
    private static <T> Set<T> load(String name, Class<T> clazz) {
        throw new AssertionError();
    }
}
