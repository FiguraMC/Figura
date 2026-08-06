package org.figuramc.figura.fsb_client;

import net.minecraft.client.Minecraft;
import org.figuramc.fsb2.api.config.ServerIdentification;
import org.figuramc.fsb2.api.utils.EventSystem;

public class FSBClientEvents extends EventSystem {
    @Override
    protected void enqueue(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    private FSBClientEvents() {
    }

    public static final FSBClientEvents INSTANCE = new FSBClientEvents();

    public static class ServerID extends Event {
        public final ServerIdentification ident;

        public ServerID(ServerIdentification ident) {
            this.ident = ident;
        }
    }

    public ReturnableEventBus<ServerID, ClientSession.ConnectionPolicy> SERVER_CONNECTED = new ReturnableEventBus<>();
    public EventBus<ServerID> SERVER_RECONFIGURED = new EventBus<>();
}
