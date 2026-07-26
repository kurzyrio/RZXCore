package com.rzx.core.gui;

import com.rzx.core.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Fluent builder for {@link Menu} instances with optional pagination support.
 */
public class MenuBuilder {

    private final String title;
    private final int size;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, BiConsumer<Player, ClickType>> clickHandlers = new HashMap<>();
    private List<ItemStack> paginatedItems = null;
    private BiConsumer<Player, ClickType> paginatedHandler = null;
    private int[] borderSlots = new int[0];

    /**
     * Creates a new MenuBuilder.
     *
     * @param title the inventory title
     * @param size  the inventory size (rounded up to nearest multiple of 9, clamped 9-54)
     */
    public MenuBuilder(String title, int size) {
        this.title = title;
        int s = size;
        if (s % 9 != 0) s = s + (9 - (s % 9));
        this.size = Math.max(9, Math.min(54, s));
    }

    /** Sets an item with a click handler at the given slot. */
    public MenuBuilder setItem(int slot, ItemStack item, BiConsumer<Player, ClickType> handler) {
        items.put(slot, item);
        if (handler != null) clickHandlers.put(slot, handler);
        return this;
    }

    /** Sets an item without a click handler. */
    public MenuBuilder setItem(int slot, ItemStack item) {
        items.put(slot, item);
        return this;
    }

    /** Fills the border of the inventory with the given item. */
    public MenuBuilder fillBorder(ItemStack item) {
        List<Integer> border = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                border.add(i);
                setItem(i, item);
            }
        }
        this.borderSlots = border.stream().mapToInt(i -> i).toArray();
        return this;
    }

    /** Sets paginated items with a shared click handler. */
    public MenuBuilder setPaginatedItems(List<ItemStack> allItems, BiConsumer<Player, ClickType> handler) {
        this.paginatedItems = allItems;
        this.paginatedHandler = handler;
        return this;
    }

    /** Builds the menu at page 0. */
    public Menu build() {
        return build(0);
    }

    /** Builds the menu at the specified page. */
    public Menu build(int page) {
        Map<Integer, ItemStack> currentItems = new HashMap<>(this.items);
        Map<Integer, BiConsumer<Player, ClickType>> currentHandlers = new HashMap<>(this.clickHandlers);
        int totalPages = 1;

        if (paginatedItems != null) {
            // Calculate available slots (not border, not already occupied, not in bottom row)
            List<Integer> availableSlots = new ArrayList<>();
            for (int i = 0; i < size - 9; i++) {
                boolean isBorder = false;
                for (int b : borderSlots) {
                    if (b == i) { isBorder = true; break; }
                }
                if (!isBorder && !currentItems.containsKey(i)) {
                    availableSlots.add(i);
                }
            }

            int itemsPerPage = availableSlots.size();
            if (itemsPerPage > 0) {
                totalPages = (int) Math.ceil((double) paginatedItems.size() / itemsPerPage);
                if (totalPages == 0) totalPages = 1;
                if (page < 0) page = 0;
                if (page >= totalPages) page = totalPages - 1;

                int startIndex = page * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, paginatedItems.size());

                int slotIndex = 0;
                for (int i = startIndex; i < endIndex; i++) {
                    int slot = availableSlots.get(slotIndex++);
                    currentItems.put(slot, paginatedItems.get(i));
                    if (paginatedHandler != null) {
                        currentHandlers.put(slot, paginatedHandler);
                    }
                }

                // Previous page button
                if (page > 0) {
                    int prevSlot = size - 6;
                    currentItems.put(prevSlot, new ItemBuilder(Material.ARROW)
                            .name(Component.text("Previous Page").color(NamedTextColor.YELLOW)).build());
                    final int prevPage = page - 1;
                    currentHandlers.put(prevSlot, (p, type) -> { p.closeInventory(); open(p, prevPage); });
                }

                // Next page button
                if (page < totalPages - 1) {
                    int nextSlot = size - 4;
                    currentItems.put(nextSlot, new ItemBuilder(Material.ARROW)
                            .name(Component.text("Next Page").color(NamedTextColor.YELLOW)).build());
                    final int nextPage = page + 1;
                    currentHandlers.put(nextSlot, (p, type) -> { p.closeInventory(); open(p, nextPage); });
                }
            }
        }

        return new Menu(title, size, currentItems, currentHandlers, page, totalPages);
    }

    /** Convenience: builds and opens the menu for the player at page 0. */
    public void open(Player player) { build().open(player); }

    /** Convenience: builds and opens at the specified page. */
    public void open(Player player, int page) { build(page).open(player); }
}

