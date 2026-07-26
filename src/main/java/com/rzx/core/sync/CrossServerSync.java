package com.rzx.core.sync;

import java.util.function.Consumer;

/**
 * Interface for cross-server synchronization (e.g. via Redis pub/sub or plugin messaging channels).
 * <p>
 * This is currently a stub â€” no real implementation exists yet. The {@link NoOpCrossServerSync}
 * default is used at runtime. When a Redis or plugin-messaging implementation is added later,
 * it will implement this interface without changing any consumer-facing API.
 */
public interface CrossServerSync {

    /**
     * Publishes a message to the given channel.
     *
     * @param channel the channel name
     * @param payload the message payload (typically JSON)
     */
    void publish(String channel, String payload);

    /**
     * Subscribes to messages on the given channel.
     *
     * @param channel the channel name
     * @param handler a consumer that receives each incoming message payload
     */
    void subscribe(String channel, Consumer<String> handler);

    /**
     * Unsubscribes from a previously subscribed channel.
     *
     * @param channel the channel name
     */
    void unsubscribe(String channel);

    /** Closes all connections and releases resources. */
    void close();
}

