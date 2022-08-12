package org.moon.figura.binary_backend.handlers;

//Not sure about this class name idea, but it's meant to go with whatever version of the protocol it was designed for.
public class MessageHandlerV0 {

    private static int i = 0;

    public static final int
    S2C_AUTH = i++,
    S2C_SYSTEM_MESSAGE = i++,
    S2C_CONNECTED = i++,
    S2C_KEEPALIVE = i++,
    S2C_TOAST = i++,
    S2C_AVATAR_PROVIDE = i++,
    S2C_USERINFO_PROVIDE = i++;




    public static NewMessageHandler get() {
        return NewMessageHandler.builder()
                .build();
    }


}
