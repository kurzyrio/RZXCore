package com.rzx.core.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Manages localization messages with MiniMessage parsing and legacy color code fallback.
 * Supports {@code %key%}-style placeholder substitution applied before parsing.
 * <p>
 * PlaceholderAPI integration is handled by HookManager, not natively here.
 */
public class MessageManager {

    private final Plugin plugin;
    private final String locale;
    private final File langFile;
    private FileConfiguration langConfig;

    /**
     * Constructs a MessageManager for a specific locale.
     *
     * @param plugin the plugin instance
     * @param locale the locale name (e.g. "en_US")
     */
    public MessageManager(Plugin plugin, String locale) {
        this.plugin = plugin;
        this.locale = locale;
        this.langFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
        load();
    }

    private void load() {
        String resourcePath = "lang/" + locale + ".yml";

        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
            } else {
                plugin.getLogger().warning("Locale file " + resourcePath + " not found in jar resources.");
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Merge defaults from JAR if available
        InputStream defaultStream = plugin.getResource(resourcePath);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultConfig);
            langConfig.options().copyDefaults(true);
            try {
                langConfig.save(langFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save merged lang defaults to " + langFile, e);
            }
        }
    }

    /** Reloads the language file from disk. */
    public void reload() {
        load();
    }

    /**
     * Gets a raw, unprocessed string message from the language file.
     *
     * @param key the configuration key
     * @return the raw string, or null if missing
     */
    public String getRawMessage(String key) {
        return langConfig.getString(key);
    }

    /**
     * Retrieves a parsed Component for the given key, applying placeholders.
     * Placeholders are provided as varargs key-value pairs:
     * {@code getMessage("key", "player", "Steve")} replaces {@code %player%} with "Steve".
     * <p>
     * Uses MiniMessage for parsing; falls back to legacy {@code &}-code translation on failure.
     *
     * @param key          the message key
     * @param placeholders key-value pairs
     * @return the parsed Component
     */
    public Component getMessage(String key, String... placeholders) {
        String text = getRawMessage(key);

        if (text == null) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize("&cMissing message: " + key);
        }

        // Apply provided placeholders
        if (placeholders != null) {
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                String pKey = "%" + placeholders[i] + "%";
                String pValue = placeholders[i + 1] != null ? placeholders[i + 1] : "";
                text = text.replace(pKey, pValue);
            }
        }

        // Resolve %prefix% one level deep
        if (text.contains("%prefix%")) {
            String prefix = getRawMessage("prefix");
            if (prefix != null) {
                text = text.replace("%prefix%", prefix);
            }
        }

        // MiniMessage parsing with legacy fallback
        try {
            if (text.contains("<") && text.contains(">")) {
                return MiniMessage.miniMessage().deserialize(text);
            }
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        } catch (Exception e) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }
}

