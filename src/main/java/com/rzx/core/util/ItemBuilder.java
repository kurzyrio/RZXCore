package com.rzx.core.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for {@link ItemStack}.
 */
public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack != null ? itemStack.clone() : new ItemStack(Material.AIR);
    }

    public ItemBuilder name(Component displayName) {
        itemStack.editMeta(meta -> meta.displayName(displayName));
        return this;
    }

    public ItemBuilder lore(Component... lines) {
        itemStack.editMeta(meta -> meta.lore(Arrays.asList(lines)));
        return this;
    }

    public ItemBuilder lore(List<Component> lines) {
        itemStack.editMeta(meta -> meta.lore(lines));
        return this;
    }

    public ItemBuilder addLore(Component line) {
        itemStack.editMeta(meta -> {
            List<Component> currentLore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            currentLore.add(line);
            meta.lore(currentLore);
        });
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        itemStack.editMeta(meta -> meta.addEnchant(enchantment, level, true));
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        itemStack.editMeta(meta -> meta.addItemFlags(flags));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        itemStack.editMeta(meta -> meta.setUnbreakable(unbreakable));
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public <Z> ItemBuilder persistentData(NamespacedKey key, PersistentDataType<?, Z> type, Z value) {
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(key, type, value));
        return this;
    }

    public ItemBuilder customModelData(int data) {
        itemStack.editMeta(meta -> meta.setCustomModelData(data));
        return this;
    }

    /** Builds and returns a clone of the constructed ItemStack. */
    public ItemStack build() {
        return itemStack.clone();
    }
}

