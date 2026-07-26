package com.rzx.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single shared listener that routes inventory clicks to the correct {@link Menu}.
 * Cancels click events by default to prevent item theft.
 */
public class MenuListener implements Listener {

    private static final Map<UUID, Menu> activeMenus = new ConcurrentHashMap<>();

    public static void registerMenu(UUID playerId, Menu menu) {
        activeMenus.put(playerId, menu);
    }

    public static void unregisterMenu(UUID playerId) {
        activeMenus.remove(playerId);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Menu menu = activeMenus.get(player.getUniqueId());
        if (menu != null) {
            event.setCancelled(true);
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                menu.handleClick(player, event.getRawSlot(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            unregisterMenu(player.getUniqueId());
        }
    }
}

