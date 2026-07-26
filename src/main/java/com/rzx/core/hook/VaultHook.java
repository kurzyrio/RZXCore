package com.rzx.core.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Wraps Vault's Economy and Permission providers.
 * Guarded by class-existence check in {@link HookManager} to avoid NoClassDefFoundError.
 */
public class VaultHook {

    private Economy economy = null;
    private Permission permission = null;

    public VaultHook(Plugin plugin) {
    }

    /** Sets up economy and permission hooks. Returns true if economy was found. */
    public boolean setup() {
        boolean hasEcon = setupEconomy();
        setupPermissions();
        return hasEcon;
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp =
                Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        permission = rsp.getProvider();
        return permission != null;
    }

    public boolean hasEconomy() { return economy != null; }
    public boolean hasPermissions() { return permission != null; }
    public Economy getEconomy() { return economy; }
    public Permission getPermissions() { return permission; }

    public double getBalance(Player player) {
        return hasEconomy() ? economy.getBalance(player) : 0.0;
    }

    public boolean withdraw(Player player, double amount) {
        return hasEconomy() && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        return hasEconomy() && economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean hasPermission(Player player, String perm) {
        return hasPermissions() ? permission.has(player, perm) : player.hasPermission(perm);
    }
}

