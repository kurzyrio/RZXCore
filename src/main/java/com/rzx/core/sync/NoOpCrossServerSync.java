package com.rzx.core.sync;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * No-op implementation of {@link CrossServerSync}.
 * Used as the default when no cross-server backend is configured.
 * Logs a warning on first use so operators know sync calls are silently dropped.
 */
public class NoOpCrossServerSync implements CrossServerSync {

    private final Logger logger;
    private boolean warned = false;

    public NoOpCrossServerSync(Logger logger) {
        this.logger = logger;
    }

    private void warnOnce() {
        if (!warned) {
            logger.warning("CrossServerSync is not configured — sync calls are being silently dropped. "
                    + "Configure a sync backend (e.g. Redis) if you need cross-server communication.");
            warned = true;
        }
    }

    @Override
    public void publish(String channel, String payload) {
        warnOnce();
    }

    @Override
    public void subscribe(String channel, Consumer<String> handler) {
        warnOnce();
    }

    @Override
    public void unsubscribe(String channel) {
        // No-op
    }

    @Override
    public void close() {
        // No-op
    }
}

