package com.rzx.core.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Immutable menu snapshot: title, size, items, click handlers, pagination state.
 */
public class Menu {

    private final String title;
    private final int size;
    private final Map<Integer, ItemStack> items;
    private final Map<Integer, BiConsumer<Player, ClickType>> clickHandlers;
    private final int currentPage;
    private final int totalPages;

    Menu(String title, int size, Map<Integer, ItemStack> items,
         Map<Integer, BiConsumer<Player, ClickType>> clickHandlers, int currentPage, int totalPages) {
        this.title = title;
        this.size = size;
        this.items = items;
        this.clickHandlers = clickHandlers;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    /** Opens this menu for the given player. */
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, Component.text(title));
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() < size) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }
        MenuListener.registerMenu(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    /** Dispatches a click to the registered handler for the given slot. */
    public void handleClick(Player player, int slot, ClickType clickType) {
        BiConsumer<Player, ClickType> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(player, clickType);
        }
    }

    public String getTitle() { return title; }
    public int getSize() { return size; }
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
}

