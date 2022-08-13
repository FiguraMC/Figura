package org.moon.figura.binary_backend;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.moon.figura.config.Config;

public class NewNetworkManager {

    public static BackendConnection currentConnection;

    public static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Config.BACKEND_IP.asString());
        return "ws://" + backendIP.getHost() + ":" + backendIP.getPort();
    }

}
