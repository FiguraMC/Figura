package org.figuramc.figura.fsb_client;

import java.util.HashMap;

/**
 * Unlike {@link org.figuramc.figura.config.Configs}, this is just a GSON-compatible object.
 * We don't do layout and i18n here; other classes are in charge of that.
 */
public class FSBClientConfig {
    /**
     * Default connection policy to apply to servers we haven't seen before.
     */
    public ClientSession.ConnectionPolicy defaultPolicy;

    /**
     * If enabled, forces the connection prompt on screen instead of waiting for the player to open the Figura menu.
     */
    public boolean intrusivePrompts;

    /**
     * Map of server IP to policy.
     * TODO: {@link net.minecraft.client.multiplayer.ServerData} ????
     */
    public HashMap<String, ClientSession.ConnectionPolicy> policies;
}
