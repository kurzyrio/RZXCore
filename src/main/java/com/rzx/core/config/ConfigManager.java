package com.rzx.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Manages YAML configuration files including loading, saving, merging defaults,
 * and version migrations. Generic and reusable across different plugins.
 */
public class ConfigManager {

    private final Plugin plugin;
    private final String filename;
    private final File configFile;
    private FileConfiguration config;
    private final TreeMap<Integer, Consumer<FileConfiguration>> migrations;

    /**
     * Constructs a new ConfigManager.
     *
     * @param plugin   the plugin whose data folder to use
     * @param filename the YAML filename (e.g. "config.yml")
     */
    public ConfigManager(Plugin plugin, String filename) {
        this.plugin = plugin;
        this.filename = filename;
        this.configFile = new File(plugin.getDataFolder(), filename);
        this.migrations = new TreeMap<>();
    }

    /**
     * Registers a migration to be applied when the config version is outdated.
     *
     * @param fromVersion the version number this migration upgrades FROM
     * @param migration   the migration logic
     */
    public void registerMigration(int fromVersion, Consumer<FileConfiguration> migration) {
        this.migrations.put(fromVersion, migration);
    }

    /**
     * Loads the configuration from disk. If the file doesn't exist, saves the default
     * resource from the plugin JAR. Merges new default keys, saves merged config,
     * then applies pending migrations.
     */
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (plugin.getResource(filename) != null) {
                plugin.saveResource(filename, false);
            } else {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create config file " + filename, e);
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Merge defaults from JAR without overwriting user values
        InputStream defaultStream = plugin.getResource(filename);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
        }

        save();
        runMigrations();
    }

    /** Saves the current FileConfiguration to disk. */
    public void save() {
        if (config == null) return;
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    /** Reloads the configuration from disk. */
    public void reload() {
        load();
    }

    /** Returns the loaded FileConfiguration. */
    public FileConfiguration getConfig() {
        return config;
    }

    private void runMigrations() {
        int currentVersion = config.getInt("config-version", 0);
        int expectedVersion = migrations.isEmpty() ? 0 : migrations.lastKey() + 1;

        if (currentVersion < expectedVersion) {
            plugin.getLogger().info("Migrating " + filename + " from version " + currentVersion + "...");
            boolean modified = false;

            for (Map.Entry<Integer, Consumer<FileConfiguration>> entry : migrations.entrySet()) {
                if (entry.getKey() >= currentVersion) {
                    plugin.getLogger().info("Applying migration v" + entry.getKey() + " -> v" + (entry.getKey() + 1));
                    try {
                        entry.getValue().accept(config);
                        currentVersion = entry.getKey() + 1;
                        config.set("config-version", currentVersion);
                        modified = true;
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to apply migration for v" + entry.getKey(), e);
                        break;
                    }
                }
            }

            if (modified) {
                save();
                plugin.getLogger().info("Migration of " + filename + " completed successfully.");
            }
        }
    }
}

