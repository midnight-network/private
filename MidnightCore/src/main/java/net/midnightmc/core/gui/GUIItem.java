package net.midnightmc.core.gui;

import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Utility class for making item of GUI
 *
 * @author HSW
 */
public class GUIItem {

    private final ItemStack itemStack;
    @Getter
    private Consumer<Player> consumer;

    public GUIItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack(Player player) {
        return itemStack;
    }

    /**
     * Set execution for this item.<br>
     * close gui for default.
     * if you don't want to close gui, use {@link #setExecute(Consumer, boolean)}
     *
     * @param consumer consumer which will be executed when this item is clicked
     * @return the item
     */
    public @NotNull GUIItem setExecute(Consumer<Player> consumer) {
        return setExecute(consumer, true);
    }

    /**
     * Set execution for this item.
     *
     * @param consumer consumer which will be executed when this item is clicked
     * @param close    whether close the gui when this item is clicked
     * @return the item
     */
    public @NotNull GUIItem setExecute(Consumer<Player> consumer, boolean close) {
        if (close) {
            this.consumer = consumer.andThen(HumanEntity::closeInventory);
        } else {
            this.consumer = consumer;
        }
        return this;
    }

}
