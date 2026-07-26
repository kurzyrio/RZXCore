package com.rzx.core.hook;

import com.rzx.core.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Detects and initializes optional plugin hooks (Vault, PlaceholderAPI).
 */
public class HookManager {

    private final Plugin plugin;
    private final PlayerDataService playerDataService;
    private VaultHook vaultHook;
    private boolean placeholderAPIEnabled;
    private boolean vaultEnabled;

    public HookManager(Plugin plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
    }

    /** Detects available hooks and initializes them. Call in onEnable(). */
    public void detectHooks() {
        // Vault
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                vaultHook = new VaultHook(plugin);
                vaultEnabled = vaultHook.setup();
                plugin.getLogger().info(vaultEnabled
                        ? "Vault integration: ENABLED"
                        : "Vault integration: Economy provider not found");
            } else {
                plugin.getLogger().info("Vault integration: NOT FOUND");
            }
        } catch (NoClassDefFoundError | Exception e) {
            plugin.getLogger().info("Vault integration: NOT FOUND");
        }

        // PlaceholderAPI
        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                PlaceholderAPIHook papiHook = new PlaceholderAPIHook(plugin, playerDataService);
                papiHook.register();
                placeholderAPIEnabled = true;
                plugin.getLogger().info("PlaceholderAPI integration: ENABLED");
            } else {
                plugin.getLogger().info("PlaceholderAPI integration: NOT FOUND");
            }
        } catch (NoClassDefFoundError | Exception e) {
            placeholderAPIEnabled = false;
            plugin.getLogger().info("PlaceholderAPI integration: NOT FOUND");
        }
    }

    public boolean isVaultEnabled() { return vaultEnabled; }
    public boolean isPlaceholderAPIEnabled() { return placeholderAPIEnabled; }
    public VaultHook getVaultHook() { return vaultHook; }

    /**
     * Resolves placeholders in text using PlaceholderAPI if available.
     */
    public String resolvePlaceholders(Player player, String text) {
        if (placeholderAPIEnabled) {
            return PlaceholderAPIHook.resolvePlaceholders(player, text);
        }
        return text;
    }
}

