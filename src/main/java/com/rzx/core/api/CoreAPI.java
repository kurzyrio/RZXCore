package com.rzx.core.api;

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
 * The public API for RZXCore.
 * <p>
 * Consumer plugins should obtain an instance via Bukkit's {@link org.bukkit.plugin.ServicesManager}:
 * <pre>{@code
 * RegisteredServiceProvider<CoreAPI> provider =
 *     getServer().getServicesManager().getRegistration(CoreAPI.class);
 * if (provider != null) {
 *     CoreAPI api = provider.getProvider();
 *     if (!api.getApiVersion().startsWith("1.")) {
 *         getLogger().severe("Incompatible RZXCore version: " + api.getApiVersion());
 *         getServer().getPluginManager().disablePlugin(this);
 *         return;
 *     }
 * }
 * }</pre>
 * Or use the convenience accessor {@code RZXCore.getAPI()}.
 */
public interface CoreAPI {

    /** Creates a new ConfigManager for the given plugin and filename. */
    ConfigManager createConfigManager(Plugin plugin, String filename);

    /** Creates a new MessageManager for the given plugin and locale. */
    MessageManager createMessageManager(Plugin plugin, String locale);

    /** Returns the shared DatabaseManager instance. */
    DatabaseManager getDatabaseManager();

    /** Returns the shared PlayerDataService instance. */
    PlayerDataService getPlayerDataService();

    /** Returns the HookManager for checking optional integrations. */
    HookManager getHookManager();

    /** Returns the shared CooldownManager instance. */
    CooldownManager getCooldownManager();

    /** Returns the CrossServerSync instance. */
    CrossServerSync getCrossServerSync();

    /** Creates a new CommandFramework for the given plugin. */
    CommandFramework createCommandFramework(Plugin plugin, MessageManager messageManager);

    /** Returns the semantic API version string (e.g. "1.0.0"). */
    String getApiVersion();
}

