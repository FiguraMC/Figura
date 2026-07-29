package org.figuramc.figura.fsb_client;

import net.minecraft.client.Minecraft;
import org.figuramc.figura.FiguraMod;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.utils.FSBLogger;
import org.figuramc.fsb2.server.internals.logging.LoggingProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FSBClientEntrypoint {
    public static @Nullable ProtocolSession CLIENT_SESSION = null;
    // TODO: non-sucky logger
    public static FSBLogger FSB_LOGGER = new LoggingProxy(FiguraMod.LOGGER);

    public static @NotNull ProtocolSession clientSession() {
        return Objects.requireNonNull(CLIENT_SESSION);
    }

    public static void init() {
        CLIENT_SESSION = new ProtocolSession(FSB_LOGGER, Minecraft.getInstance(), true);
    }
}
