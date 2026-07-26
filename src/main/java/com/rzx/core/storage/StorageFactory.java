package com.rzx.core.storage;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

/**
 * Factory that creates the appropriate {@link DatabaseManager} implementation
 * based on a YAML {@code storage:} configuration section.
 */
public class StorageFactory {

    /**
     * Creates a DatabaseManager based on the config.
     *
     * @param config     the 'storage' config section
     * @param dataFolder the plugin's data folder (for SQLite file location)
     * @return a configured DatabaseManager instance
     */
    public static DatabaseManager create(ConfigurationSection config, File dataFolder) {
        String type = config.getString("type", "sqlite");
        int maxPoolSize = config.getInt("pool.maximum-pool-size", 10);

        if ("mysql".equalsIgnoreCase(type)) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String database = config.getString("mysql.database", "rzxcore");
            String username = config.getString("mysql.username", "root");
            String password = config.getString("mysql.password", "");
            return new MySQLDatabase(host, port, database, username, password, maxPoolSize);
        } else {
            return new SQLiteDatabase(dataFolder, maxPoolSize);
        }
    }
}

