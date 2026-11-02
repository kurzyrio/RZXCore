package com.rzx.core.data;

import com.rzx.core.storage.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data with an in-memory cache backed by async database I/O.
 * <p>
 * The cache uses {@link ConcurrentHashMap} for thread-safe access between the main
 * thread and async scheduler tasks. Dirty tracking avoids unnecessary writes.
 */
public class PlayerDataService {

    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();

    public PlayerDataService(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /** Creates the required database tables if they don't exist. */
    public void initialize() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS player_data ("
                    + "uuid VARCHAR(36) PRIMARY KEY, "
                    + "username VARCHAR(16) NOT NULL, "
                    + "first_join BIGINT NOT NULL, "
                    + "last_seen BIGINT NOT NULL"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS player_data_extra ("
                    + "uuid VARCHAR(36) NOT NULL, "
                    + "namespace VARCHAR(64) NOT NULL, "
                    + "data_key VARCHAR(128) NOT NULL, "
                    + "data_value TEXT, "
                    + "PRIMARY KEY (uuid, namespace, data_key)"
                    + ")");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a player's data from the database asynchronously and caches it.
     * If no record exists, creates a new entry with the current timestamp.
     */
    public void loadPlayer(UUID uuid, String username) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try (Connection conn = databaseManager.getConnection()) {
                PlayerData playerData = null;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM player_data WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            playerData = new PlayerData(
                                    uuid,
                                    rs.getString("username"),
                                    rs.getLong("first_join"),
                                    rs.getLong("last_seen")
                            );
                        }
                    }
                }

                if (playerData != null) {
                    // Load extra data
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT namespace, data_key, data_value FROM player_data_extra WHERE uuid = ?")) {
                        stmt.setString(1, uuid.toString());
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                playerData.setExtraData(
                                        rs.getString("namespace"),
                                        rs.getString("data_key"),
                                        rs.getString("data_value")
                                );
                            }
                        }
                    }
                    // Update username if changed
                    playerData.setUsername(username);
                    playerData.setLastSeen(System.currentTimeMillis());
                    dirtyPlayers.add(uuid);
                } else {
                    long now = System.currentTimeMillis();
                    playerData = new PlayerData(uuid, username, now, now);
                    dirtyPlayers.add(uuid);
                }

                cache.put(uuid, playerData);

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player data for " + uuid + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /** Saves a single player's data asynchronously. */
    public void savePlayer(UUID uuid) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> performSave(uuid));
    }

    /** Saves ALL cached player data synchronously (for server shutdown). */
    public void saveAllPlayers() {
        for (UUID uuid : cache.keySet()) {
            performSave(uuid);
        }
    }

    private void performSave(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        if (!dirtyPlayers.contains(uuid)) return;

        // Remove from dirty set first so concurrent modifications re-mark it as dirty
        dirtyPlayers.remove(uuid);

        try (Connection conn = databaseManager.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Upsert player_data using UPDATE-then-INSERT for dialect compatibility
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE player_data SET username = ?, last_seen = ? WHERE uuid = ?")) {
                    update.setString(1, data.getUsername());
                    update.setLong(2, data.getLastSeen());
                    update.setString(3, uuid.toString());
                    int affected = update.executeUpdate();

                    if (affected == 0) {
                        try (PreparedStatement insert = conn.prepareStatement(
                                "INSERT INTO player_data (uuid, username, first_join, last_seen) VALUES (?, ?, ?, ?)")) {
                            insert.setString(1, uuid.toString());
                            insert.setString(2, data.getUsername());
                            insert.setLong(3, data.getFirstJoin());
                            insert.setLong(4, data.getLastSeen());
                            insert.executeUpdate();
                        }
                    }
                }

                // Upsert extra data
                for (Map.Entry<String, Map<String, String>> nsEntry : data.getAllExtraData().entrySet()) {
                    String namespace = nsEntry.getKey();
                    for (Map.Entry<String, String> dataEntry : nsEntry.getValue().entrySet()) {
                        String dataKey = dataEntry.getKey();
                        String dataValue = dataEntry.getValue();

                        try (PreparedStatement update = conn.prepareStatement(
                                "UPDATE player_data_extra SET data_value = ? WHERE uuid = ? AND namespace = ? AND data_key = ?")) {
                            update.setString(1, dataValue);
                            update.setString(2, uuid.toString());
                            update.setString(3, namespace);
                            update.setString(4, dataKey);
                            int affected = update.executeUpdate();

                            if (affected == 0) {
                                try (PreparedStatement insert = conn.prepareStatement(
                                        "INSERT INTO player_data_extra (uuid, namespace, data_key, data_value) VALUES (?, ?, ?, ?)")) {
                                    insert.setString(1, uuid.toString());
                                    insert.setString(2, namespace);
                                    insert.setString(3, dataKey);
                                    insert.setString(4, dataValue);
                                    insert.executeUpdate();
                                }
                            }
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                dirtyPlayers.add(uuid); // Re-flag dirty on failure
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player data for " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Saves player data async and removes from cache on quit. */
    public void handlePlayerQuit(UUID uuid) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            performSave(uuid);
            cache.remove(uuid);
        });
    }

    /** Gets cached player data (may be null if not yet loaded). */
    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    /** Gets a specific extra data value for a player. */
    public String getExtraData(UUID uuid, String namespace, String key) {
        PlayerData data = cache.get(uuid);
        return data != null ? data.getExtraData(namespace, key) : null;
    }

    /** Sets extra data and marks the player as dirty. */
    public void setExtraData(UUID uuid, String namespace, String key, Object value) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.setExtraData(namespace, key, String.valueOf(value));
            dirtyPlayers.add(uuid);
        }
    }
}

