package org.moon.figura.binary_backend;

import com.google.common.collect.ImmutableMap;
import org.moon.figura.FiguraMod;
import org.moon.figura.binary_backend.packets.PacketType;
import org.moon.figura.binary_backend.packets.server2client.AbstractS2CPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 *
 * :::PACKET PROTOCOL:::
 * We first send a VarInt containing the TYPE of the Packet.
 * Then, we send the data.
 * The server and client both agree ahead of time on the different types and their formatting.
 *
 * For example, in this fictional idea:
 * We want to send an __S2C Avatar Packet__.
 * Both the client and server agree that an __S2C Avatar Packet__ has the type 106. (Random number)
 * They also agree that an __S2C Avatar Packet__ consists of a String name, then a ByteArray of data.
 * So we send the number 106, then we send (String owner), then we send (ByteArray data).
 *
 * However, how do we send String owner?
 * The server and client agree that a String is a VarInt length (in bytes), then a bunch of bytes in utf-8.
 *
 * How do we send ByteArray data?
 * Server and client agree that a ByteArray is a VarInt length, then a bunch of bytes making up the array.
 *
 * Let's say we send an avatar message where the owner is "Fran" (normally would be uuid, but ignore), and the avatar byte data is [1, 2, 3, 4, 5].
 * So our final message, after translation, is this.
 *
 * //Translate the avatar packet into its code number, then its sub objects.
 * (Avatar Packet ("Fran", [1, 2, 3, 4, 5])) --> 106, "Fran", [1, 2, 3, 4, 5]
 *
 * //First sub object is a string "Fran", so we convert "Fran" string into bytes.
 * "Fran" --> string length, bytes of string --> 4, 'F', 'r', 'a', 'n'.
 *
 * //Next sub object is a byte array, so we convert this into bytes.
 * [1, 2, 3, 4, 5] --> arr.length, bytes of arr --> 5, 1, 2, 3, 4, 5.
 *
 * Put together, the whole message is:
 * 106 4 'F' 'r' 'a' 'n' 5 1 2 3 4 5
 *
 * Now, we're going to act as the client, and parse this string one byte at a time from the other side, reconstructing the original avatar message.
 * Read a VarInt: we get 106. Alright, that means this is going to be an S2C avatar message, cool.
 * We know what an S2C avatar message looks like, it looks like (String, ByteArray), so we're going to look for a String now.
 * String:
 *      Read a VarInt: We get 4. Alright, so we know this string has length 4, so we're going to read 4 more bytes.
 *      Read 4 more bytes: we get 'F' 'r' 'a' 'n'. Then since we know this is a String, we put those together to get "Fran."
 * Now we read the string, so we're going to now read a ByteArray.
 * ByteArray:
 *      Read a VarInt: We get 5. We now know this byte array is 5 long.
 *      Read 5 more bytes. We get the byte array [1, 2, 3, 4, 5].
 * We've now read the incoming message of bytes, and figured out that it was an Avatar Message, and it has owner = "Fran", and data = [1, 2, 3, 4, 5].
 *
 *
 *
 */
public class NewMessageHandler {

    /**
     * A list of actions pending being run. Actions are added to the queue by the backend thread,
     * then later run at consistent times on the main thread.
     */
    private final BlockingQueue<Runnable> pendingAction;

    public Thread messageParseThread;

    /**
     * A map from integer Message Type Code, to MessageParser which will convert that type code into a runnable.
     * This runnable is then added to the pendingAction queue.
     */
    private final ImmutableMap<Integer, TypeHandler<?>> codeToTypeHandler;

    private NewMessageHandler(Builder builder) {
        pendingAction = new LinkedBlockingQueue<>();
        codeToTypeHandler = builder.buildMap();
    }

    /**
     * Takes this message and adds it to the queue of pending messages.
     * @param message
     */
    public void acceptMessage(ByteBuffer message) {
        try {
            final int typeCode = ByteStreamConverter.readVarInt(message);
            TypeHandler<?> handler = codeToTypeHandler.get(typeCode);
            if (handler == null)
                throw new IOException("No handler exists for type code " + typeCode);
            Runnable action = handler.parseToAction(message);
            pendingAction.put(action);
        } catch (IOException e) {
            FiguraMod.LOGGER.warn("Failed to parse message", e);
        } catch (InterruptedException e) {
            FiguraMod.LOGGER.warn("Message Parser was interrupted", e);
        }
    }

    public void queueAction(Runnable action) {
        try {
            pendingAction.put(action);
        } catch (InterruptedException e) {
            FiguraMod.LOGGER.warn("Failed to queue network action", e);
        }
    }

    public void flushActionQueue() {
        while (!pendingAction.isEmpty()) {
            try {
                pendingAction.take().run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableMap.Builder<Integer, TypeHandler<?>> mapBuilder = ImmutableMap.builder();

        public <T extends AbstractS2CPacket> Builder register(PacketType<T> packetType, Consumer<T> handler) {
            mapBuilder.put(packetType.id, new TypeHandler<>(packetType.constructor, handler));
            return this;
        }

        public NewMessageHandler build() {
            return new NewMessageHandler(this);
        }

        private ImmutableMap<Integer, TypeHandler<?>> buildMap() {
            return mapBuilder.build();
        }
    }

    /**
     * Handles one type of message
     */
    public static class TypeHandler<T extends AbstractS2CPacket> {

        private final Supplier<T> supplier;
        private final Consumer<T> handler;

        public TypeHandler(Supplier<T> constructor, Consumer<T> handler) {
            this.supplier = constructor;
            this.handler = handler;
        }

        public Runnable parseToAction(ByteBuffer buf) throws IOException {
            T packet = supplier.get();
            packet.fill(buf);
            return () -> handler.accept(packet);
        }
    }

}
