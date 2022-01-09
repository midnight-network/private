package net.midnightmc.core.gui;

import lombok.Getter;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class GUI implements InventoryHolder {

    private final ArrayList<GUIItem> items;
    @Getter
    private final InventoryType type;
    @Getter
    private final int rows;
    @Getter
    private String title;

    /**
     * @param title the title of this gui
     * @param rows  the rows count of this gui\nmax:6
     */
    public GUI(String title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows count must be higher than 0 and lower than 7");
        }
        this.type = InventoryType.CHEST;
        this.rows = rows;
        this.title = title;
        items = new ArrayList<>(rows * 9);
        for (int i = 0; i < rows * 9; i++) {
            items.add(new GUIItem(new ItemStack(Material.AIR)));
        }
    }

    public GUI(String title, InventoryType type) {
        this.type = type;
        this.rows = type.getDefaultSize();
        this.title = title;
        items = new ArrayList<>(rows);
    }

    /**
     * Set title of this GUI
     *
     * @param title the title
     */
    public GUI setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set item in this gui
     *
     * @param slot the slot number where the item will be at. must be higher than 0.
     * @param item the item
     */
    public GUI setItem(int slot, GUIItem item) {
        if (slot < 0 || slot >= rows * 9) {
            throw new IllegalArgumentException("illegal slot number");
        }
        items.set(slot, item);
        return this;
    }

    public GUI setItem(GUIItem item, int... slot) {
        for (int i : slot) {
            setItem(i, item);
        }
        return this;
    }

    public @Nullable GUIItem getItem(int i) {
        if (i > items.size()) {
            return null;
        }
        return items.get(i);
    }

    public void open(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (type == InventoryType.CHEST) {
            Inventory inv = Bukkit.createInventory(this, 9 * rows, MessageUtil.getComponent(title));
            for (int i = 0; i < items.size(); i++) {
                inv.setItem(i, items.get(i).getItemStack(player));
            }
            player.openInventory(inv);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

}
