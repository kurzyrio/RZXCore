package com.rzx.core;

import com.rzx.core.api.CoreAPI;
import com.rzx.core.config.ConfigManager;
import com.rzx.core.data.PlayerDataService;
import com.rzx.core.gui.MenuListener;
import com.rzx.core.hook.HookManager;
import com.rzx.core.impl.CoreAPIImpl;
import com.rzx.core.lang.MessageManager;
import com.rzx.core.storage.DatabaseManager;
import com.rzx.core.storage.StorageFactory;
import com.rzx.core.util.CooldownManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RZXCore â€” shared core library plugin for the Kurz plugin network.
 * <p>
 * Other plugins should depend on this plugin (via {@code plugin.yml}: {@code depend: [RZXCore]})
 * and access its services through the {@link CoreAPI} interface obtained via:
 * <ul>
 *   <li>Bukkit's {@link org.bukkit.plugin.ServicesManager} (recommended)</li>
 *   <li>The static convenience method {@link #getAPI()}</li>
 * </ul>
 */
public class RZXCore extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataService playerDataService;
    private HookManager hookManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        // 1. Configuration
        configManager = new ConfigManager(this, "config.yml");
        configManager.load();

        // 2. Language
        String locale = configManager.getConfig().getString("locale", "en_US");
        new MessageManager(this, locale);

        // 3. Storage
        ConfigurationSection storageSection = configManager.getConfig().getConfigurationSection("storage");
        if (storageSection == null) {
            getLogger().severe("Missing 'storage' section in config.yml! Using SQLite defaults.");
            configManager.getConfig().createSection("storage");
            configManager.getConfig().set("storage.type", "sqlite");
            configManager.getConfig().set("storage.pool.maximum-pool-size", 10);
            storageSection = configManager.getConfig().getConfigurationSection("storage");
        }
        databaseManager = StorageFactory.create(storageSection, getDataFolder());
        try {
            databaseManager.connect();
            getLogger().info("Database connection established successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 4. Player Data Service
        playerDataService = new PlayerDataService(this, databaseManager);
        playerDataService.initialize();

        // 5. Cooldown Manager
        cooldownManager = new CooldownManager();

        // 6. Hooks (Vault, PlaceholderAPI)
        hookManager = new HookManager(this, playerDataService);
        hookManager.detectHooks();

        // 7. Event Listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        // 8. Register API via ServicesManager
        CoreAPI api = new CoreAPIImpl(databaseManager, playerDataService, hookManager);
        getServer().getServicesManager().register(CoreAPI.class, api, this, ServicePriority.Normal);

        getLogger().info("RZXCore v" + getPluginMeta().getVersion() + " enabled successfully!");
        getLogger().info("API version: " + api.getApiVersion());
    }

    @Override
    public void onDisable() {
        // 1. Save all player data synchronously
        if (playerDataService != null) {
            getLogger().info("Saving all player data...");
            playerDataService.saveAllPlayers();
        }

        // 2. Close database
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Database connection closed.");
        }

        // 3. Unregister services
        getServer().getServicesManager().unregisterAll(this);

        getLogger().info("RZXCore disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerDataService.loadPlayer(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName()
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataService.handlePlayerQuit(event.getPlayer().getUniqueId());
        cooldownManager.clearCooldowns(event.getPlayer().getUniqueId());
    }

    /**
     * Static convenience accessor for the RZXCore API.
     * Wraps the Bukkit ServicesManager lookup.
     *
     * @return the CoreAPI instance, or null if RZXCore is not loaded
     */
    public static CoreAPI getAPI() {
        RegisteredServiceProvider<CoreAPI> provider =
                org.bukkit.Bukkit.getServicesManager().getRegistration(CoreAPI.class);
        return provider != null ? provider.getProvider() : null;
    }
}

