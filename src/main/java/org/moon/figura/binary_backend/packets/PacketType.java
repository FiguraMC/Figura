package org.moon.figura.binary_backend.packets;

import org.moon.figura.binary_backend.packets.server2client.S2CAuthPacket;
import org.moon.figura.binary_backend.packets.server2client.S2CConnectedPacket;
import org.moon.figura.binary_backend.packets.server2client.S2CKeepalivePacket;
import org.moon.figura.binary_backend.packets.server2client.S2CSystemMessagePacket;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketType<T extends AbstractPacket> {

    public static final PacketType<S2CAuthPacket> S2C_AUTH = new PacketType<>(100, S2CAuthPacket::new);
    public static final PacketType<S2CSystemMessagePacket> S2C_SYSTEM_MESSAGE = new PacketType<>(101, S2CSystemMessagePacket::new);
    public static final PacketType<S2CConnectedPacket> S2C_CONNECTED = new PacketType<>(102, S2CConnectedPacket::new);
    public static final PacketType<S2CKeepalivePacket> S2C_KEEPALIVE = new PacketType<>(103, S2CKeepalivePacket::new);

    public final Supplier<T> constructor;
    public final int id;

    private static final Set<Integer> TAKEN_IDS = new HashSet<>();

    private PacketType(int id, Supplier<T> constructor) {
        if (TAKEN_IDS.contains(id))
            throw new IllegalArgumentException("Two packets registered with the same int id " + id);
        this.constructor = constructor;
        this.id = id;
        TAKEN_IDS.add(id);
    }
}
