package com.rzx.core.impl;

import com.rzx.core.api.CoreAPI;
import com.rzx.core.command.CommandFramework;
import com.rzx.core.config.ConfigManager;
import com.rzx.core.data.PlayerDataService;
import com.rzx.core.hook.HookManager;
import com.rzx.core.lang.MessageManager;
import com.rzx.core.storage.DatabaseManager;
import com.rzx.core.sync.CrossServerSync;
import com.rzx.core.util.CooldownManager;
import org.bukkit.plugin.Plugin;

/**
 * Default implementation of {@link CoreAPI}.
 * Instantiated and registered with ServicesManager by {@link com.rzx.core.RZXCore}.
 */
public class CoreAPIImpl implements CoreAPI {

    private static final String API_VERSION = "1.0.0";

    private final DatabaseManager databaseManager;
    private final PlayerDataService playerDataService;
    private final HookManager hookManager;
    private final CooldownManager cooldownManager;
    private final CrossServerSync crossServerSync;

    public CoreAPIImpl(DatabaseManager databaseManager,
                       PlayerDataService playerDataService,
                       HookManager hookManager,
                       CooldownManager cooldownManager,
                       CrossServerSync crossServerSync) {
        this.databaseManager = databaseManager;
        this.playerDataService = playerDataService;
        this.hookManager = hookManager;
        this.cooldownManager = cooldownManager;
        this.crossServerSync = crossServerSync;
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
    public CooldownManager getCooldownManager() { return cooldownManager; }

    @Override
    public CrossServerSync getCrossServerSync() { return crossServerSync; }

    @Override
    public CommandFramework createCommandFramework(Plugin plugin, MessageManager messageManager) {
        return new CommandFramework(plugin, messageManager);
    }

    @Override
    public String getApiVersion() { return API_VERSION; }
}

