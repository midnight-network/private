package net.midnightmc.core.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Easily create itemstacks, without messing your hands.
 *
 * @author _LittleGiant_
 */
public class ItemBuilder {

    private final ItemStack is;

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m The material to create the ItemBuilder with.
     */
    public ItemBuilder(@Nullable Material m) {
        this(m, 1);
    }

    /**
     * Create a new ItemBuilder over an existing itemstack.
     *
     * @param is The itemstack to create the ItemBuilder over.
     */
    public ItemBuilder(@Nullable ItemStack is) {
        this.is = is == null ? new ItemStack(Material.AIR) : is;
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m      The material of the item.
     * @param amount The amount of the item.
     */
    public ItemBuilder(@Nullable Material m, int amount) {
        is = new ItemStack(m == null ? Material.AIR : m, amount);
    }

    /**
     * Get colored wool
     *
     * @param color The color to set the wool item to.
     */
    public static ItemBuilder getColoredWool(String color) {
        return new ItemBuilder(Material.valueOf(color + "_WOOL"));
    }

    /**
     * Get colored wool
     *
     * @param color The DyeColor to set the wool item to.
     */
    public static ItemBuilder getColoredWool(DyeColor color) {
        return new ItemBuilder(Material.valueOf(color.name() + "_WOOL"));
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     *
     * @param texture The uuid of the skull's owner.
     */
    public static ItemBuilder getSkull(String texture) {
        ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(texture.hashCode(), texture.hashCode());
        //noinspection deprecation
        Bukkit.getUnsafe().modifyItemStack(builder.is,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + texture + "\"}]}}}"
        );
        return builder;
    }

    /**
     * Clone the ItemBuilder into a new one.
     *
     * @return The cloned instance.
     */
    @Override
    public ItemBuilder clone() {
        try {
            return (ItemBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            return new ItemBuilder(is);
        }
    }

    /**
     * Set the displayname of the item.
     *
     * @param name The name to change it to.
     */
    public ItemBuilder setName(Component name) {
        ItemMeta im = is.getItemMeta();
        if (im == null) {
            return this;
        }
        im.displayName(name);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Set the displayname of the item.
     *
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        if (im == null) {
            return this;
        }
        im.displayName(MessageUtil.getComponent(name));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        ItemMeta meta = is.getItemMeta();
        meta.addItemFlags(flags);
        is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder guiMode() {
        return addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
    }

    /**
     * Add an unsafe enchantment.
     *
     * @param ench  The enchantment to add.
     * @param level The level to put the enchantment on.
     */
    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        is.addUnsafeEnchantment(ench, level);
        return this;
    }

    /**
     * Remove a certain enchant from the item.
     *
     * @param ench The enchantment to remove
     */
    public ItemBuilder removeEnchantment(Enchantment ench) {
        is.removeEnchantment(ench);
        return this;
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     *
     * @param uuid The uuid of the skull's owner.
     */
    public ItemBuilder setSkullOwner(UUID uuid) {
        try {
            SkullMeta im = (SkullMeta) is.getItemMeta();
            if (im == null) {
                return this;
            }
            im.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            is.setItemMeta(im);
        } catch (ClassCastException ignored) {
        }
        return this;
    }

    /**
     * Add an enchantment to the item.
     *
     * @param ench  The enchantment to add
     * @param level The level
     */
    public ItemBuilder addEnchant(@Nullable Enchantment ench, int level) {
        if (ench == null) {
            return this;
        }
        if (level < ench.getStartLevel() || level > ench.getMaxLevel()) {
            return this;
        }
        is.addEnchantment(ench, level);
        return this;
    }

    /**
     * Add multiple enchants at once.
     *
     * @param enchantments The enchants to add.
     */
    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        is.addEnchantments(enchantments);
        return this;
    }

    public List<Component> getLore() {
        return Objects.requireNonNullElse(is.lore(), new ArrayList<>());
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(String... lore) {
        is.lore(Arrays.stream(lore).map(MessageUtil::getComponent).toList());
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(Component... lore) {
        is.lore(Arrays.asList(lore));
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param line The line to remove.
     */
    public ItemBuilder removeLoreLine(String line) {
        List<Component> lore = getLore();
        lore.remove(MessageUtil.getComponent(line));
        is.lore(lore);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param index The index of the lore line to remove.
     */
    public ItemBuilder removeLoreLine(int index) {
        List<Component> lore = getLore();
        if (index < 0 || index > lore.size()) return this;
        lore.remove(index);
        is.lore(lore);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(String line) {
        List<Component> lore = getLore();
        lore.add(MessageUtil.getComponent(line));
        is.lore(lore);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param pos  The index of where to put it.
     * @param line The lore line to add.
     */
    public ItemBuilder setLoreLine(int pos, String line) {
        List<Component> lore = getLore();
        lore.set(pos, MessageUtil.getComponent(line));
        is.lore(lore);
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     *
     * @param color The color to set it to.
     */
    public ItemBuilder setLeatherArmorColor(Color color) {
        if (!(is.getItemMeta() instanceof LeatherArmorMeta im)) {
            return this;
        }
        im.setColor(color);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     *
     * @param color The color to set it to.
     */
    public ItemBuilder setLeatherArmorColor(@Nullable DyeColor color) {
        return setLeatherArmorColor(color == null ? null : color.getColor());
    }

    /**
     * Make this itemstack unbreakable
     */
    public ItemBuilder setUnbreakable() {
        ItemMeta im = is.getItemMeta();
        if (im == null) {
            return this;
        }
        im.setUnbreakable(true);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Retrieves the itemstack from the ItemBuilder.
     *
     * @return The itemstack created/modified by the ItemBuilder instance.
     */
    public ItemStack build() {
        return is;
    }

}