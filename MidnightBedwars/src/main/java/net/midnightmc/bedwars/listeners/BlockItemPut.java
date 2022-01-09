package net.midnightmc.bedwars.listeners;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BlockItemPut implements Listener {

    private static boolean isBlocked(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return isBlocked(itemStack.getType());
    }

    private static boolean isBlocked(Material material) {
        return MaterialTags.SWORDS.isTagged(material) || MaterialTags.AXES.isTagged(material) || MaterialTags.PICKAXES.isTagged(material);
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        Inventory clicked = e.getClickedInventory();
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            e.setCancelled(true);
        } else if (e.getClick().isShiftClick() && clicked == e.getWhoClicked().getInventory() && isBlocked(e.getCurrentItem())) {
            e.setCancelled(true);
        } else if (clicked != e.getWhoClicked().getInventory() && isBlocked(e.getCursor())) {
            e.setCancelled(true);
        } else if (e.getClick() == ClickType.NUMBER_KEY && clicked != e.getWhoClicked().getInventory()
                && isBlocked(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        ItemStack dragged = event.getOldCursor();
        if (isBlocked(dragged.getType())) {
            int inventorySize = event.getInventory().getSize();
            for (int i : event.getRawSlots()) {
                if (i < inventorySize) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

}
