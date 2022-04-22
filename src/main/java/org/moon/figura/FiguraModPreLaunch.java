package org.moon.figura;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * Exists as an injection point pre-launch.
 * Is used for loading RenderDoc in development.
 */
public class FiguraModPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
    }
}