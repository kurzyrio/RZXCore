package com.rzx.core.data;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds per-player data in a thread-safe structure.
 * <p>
 * Timestamps ({@code firstJoin}, {@code lastSeen}) are stored as epoch milliseconds.
 * Extra data is stored as a two-level map: namespace &rarr; key &rarr; serialized value.
 */
public class PlayerData {

    private final UUID uuid;
    private String username;
    private final long firstJoin;
    private long lastSeen;
    private final Map<String, Map<String, String>> extraData;

    public PlayerData(UUID uuid, String username, long firstJoin, long lastSeen) {
        this.uuid = uuid;
        this.username = username;
        this.firstJoin = firstJoin;
        this.lastSeen = lastSeen;
        this.extraData = new ConcurrentHashMap<>();
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public long getFirstJoin() { return firstJoin; }
    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public String getExtraData(String namespace, String key) {
        Map<String, String> ns = extraData.get(namespace);
        return ns != null ? ns.get(key) : null;
    }

    public void setExtraData(String namespace, String key, String value) {
        extraData.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public Map<String, Map<String, String>> getAllExtraData() {
        return Collections.unmodifiableMap(extraData);
    }
}

