package com.rzx.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single shared listener that routes inventory clicks to the correct {@link Menu}.
 * Cancels click and drag events by default to prevent item theft.
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

        Inventory topInventory = event.getView().getTopInventory();
        Menu menu = null;

        if (topInventory.getHolder() instanceof Menu m) {
            menu = m;
        } else {
            menu = activeMenus.get(player.getUniqueId());
        }

        if (menu != null) {
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInventory)) {
                menu.handleClick(player, event.getRawSlot(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInventory = event.getView().getTopInventory();
        boolean isMenu = topInventory.getHolder() instanceof Menu || activeMenus.containsKey(player.getUniqueId());

        if (isMenu) {
            for (int slot : event.getRawSlots()) {
                if (slot < topInventory.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Menu menu = activeMenus.get(player.getUniqueId());
            if (menu != null && (menu.getInventory() == null || menu.getInventory().equals(event.getInventory()))) {
                unregisterMenu(player.getUniqueId());
            }
        }
    }
}

