package com.rzx.core.impl;

import com.rzx.core.api.CoreAPI;
import com.rzx.core.config.ConfigManager;
import com.rzx.core.data.PlayerDataService;
import com.rzx.core.hook.HookManager;
import com.rzx.core.lang.MessageManager;
import com.rzx.core.storage.DatabaseManager;
import org.bukkit.plugin.Plugin;

/**
 * Default implementation of {@link CoreAPI}.
 * Instantiated and registered with ServicesManager by {@link com.kurz.core.RZXCore}.
 */
public class CoreAPIImpl implements CoreAPI {

    private static final String API_VERSION = "1.0.0";

    private final DatabaseManager databaseManager;
    private final PlayerDataService playerDataService;
    private final HookManager hookManager;

    public CoreAPIImpl(DatabaseManager databaseManager,
                       PlayerDataService playerDataService,
                       HookManager hookManager) {
        this.databaseManager = databaseManager;
        this.playerDataService = playerDataService;
        this.hookManager = hookManager;
    }

    @Override
    public ConfigManager createConfigManager(Plugin plugin, String filename) {
        return new ConfigManager(plugin, filename);
    }

    @Override
    public MessageManager createMessageManager(Plugin plugin, String locale) {
        return new MessageManager(plugin, locale);
    }

    @Override
    public DatabaseManager getDatabaseManager() { return databaseManager; }

    @Override
    public PlayerDataService getPlayerDataService() { return playerDataService; }

    @Override
    public HookManager getHookManager() { return hookManager; }

    @Override
    public String getApiVersion() { return API_VERSION; }
}

