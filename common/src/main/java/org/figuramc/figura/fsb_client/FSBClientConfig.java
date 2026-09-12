package org.figuramc.figura.fsb_client;

import java.util.HashMap;

/**
 * Unlike {@link org.figuramc.figura.config.Configs}, this is just a GSON-compatible object.
 * We don't do layout and i18n here; other classes are in charge of that.
 */
public class FSBClientConfig {
    /**
     * If enabled, forces the connection prompt on screen instead of waiting for the player to open the Figura menu.
     */
    public boolean intrusivePrompts;
}
