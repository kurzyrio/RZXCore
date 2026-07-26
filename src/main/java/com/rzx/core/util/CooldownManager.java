package com.rzx.core.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe per-player, per-action cooldown tracking.
 */
public class CooldownManager {

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Sets a cooldown for the given player and action.
     *
     * @param uuid           player UUID
     * @param action         action identifier
     * @param durationMillis cooldown duration in milliseconds
     */
    public void setCooldown(UUID uuid, String action, long durationMillis) {
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(action, System.currentTimeMillis() + durationMillis);
    }

    /** Returns true if the player is on cooldown for the action. */
    public boolean isOnCooldown(UUID uuid, String action) {
        return getRemaining(uuid, action) > 0;
    }

    /** Returns milliseconds remaining, or 0 if not on cooldown. */
    public long getRemaining(UUID uuid, String action) {
        ConcurrentHashMap<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(action);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            playerCooldowns.remove(action);
            return 0;
        }
        return remaining;
    }

    /** Removes all cooldowns for a player (call on quit). */
    public void clearCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
    }
}

