package org.moon.figura.binary_backend.handlers;

import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * :::Message protocol:::
 *
 * The format of a message is this.
 *
 * Message Type Code - VarInt - A number indicating the type of this message.
 * Size - VarInt - The size of the payload in bytes.
 * Payload - byte[] - A bunch of bytes. Using the Message Type Code,
 * we can parse these bytes in the correct way.
 *
 * This is the basic format of ANY message between a backend and a client.
 */
public class NewMessageHandler {

    /**
     * The list of messages pending parsing. These will be asynchronously turned into
     * Runnable instances by the backend thread, which are then added to the pendingAction queue.
     *
     * Runnables in the pendingAction queue are then run at consistent times on the main thread.
     */
    private final Queue<ByteBuffer> pendingParse, pendingAction;

    /**
     * A map from integer Message Type Code, to MessageParser which will convert that type code into a runnable.
     * This runnable is then added to the pendingAction queue.
     */
    private final ImmutableMap<Integer, MessageParser> typeCodeToParser;

    private NewMessageHandler(Builder builder) {
        pendingParse = new ConcurrentLinkedQueue<>();
        pendingAction = new ConcurrentLinkedQueue<>();
        typeCodeToParser = builder.buildMap();
    }

    /**
     * Takes this message and adds it to the queue of pending messages.
     * @param message
     */
    public final void acceptMessage(ByteBuffer message) {
        pendingParse.add(message);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableMap.Builder<Integer, MessageParser> mapBuilder = ImmutableMap.builder();

        Builder register(int typeCode, MessageParser parser) {
            mapBuilder.put(typeCode, parser);
            return this;
        }

        public NewMessageHandler build() {
            return new NewMessageHandler(this);
        }

        private ImmutableMap<Integer, MessageParser> buildMap() {
            return mapBuilder.build();
        }
    }

    @FunctionalInterface
    public interface MessageParser {
        Runnable parseMessage();
    }

}
