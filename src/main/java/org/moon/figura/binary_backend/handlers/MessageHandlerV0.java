package org.moon.figura.binary_backend.handlers;

import org.moon.figura.binary_backend.NewMessageHandler;
import org.moon.figura.binary_backend.packets.PacketType;
import org.moon.figura.binary_backend.packets.server2client.S2CAuthPacket;
import org.moon.figura.binary_backend.packets.server2client.S2CConnectedPacket;
import org.moon.figura.binary_backend.packets.server2client.S2CSystemMessagePacket;

//Not sure about this class name idea, but it's meant to go with whatever version of the protocol it was designed for.
public class MessageHandlerV0 {

    public static NewMessageHandler get() {
        return NewMessageHandler.builder()
                .register(PacketType.S2C_AUTH, MessageHandlerV0::handleAuth)
                .register(PacketType.S2C_SYSTEM_MESSAGE, MessageHandlerV0::handleSystemMessage)
                .register(PacketType.S2C_CONNECTED, MessageHandlerV0::handleConnected)
                .build();
    }

    /*

    These methods will be run BY THE MAIN THREAD later!

     */
    private static void handleAuth(S2CAuthPacket packet) {
        //Do stuff with the packet
    }

    private static void handleSystemMessage(S2CSystemMessagePacket packet) {
        //Do stuff with the packet
    }

    private static void handleConnected(S2CConnectedPacket packet) {
        //Do stuff with the packet
    }

}
