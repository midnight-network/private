package net.midnightmc.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BedwarsShopUtil {

    private BedwarsShopUtil() {}

    public static int requiredAmount(Player player, BedwarsItem.Price price) {
        if (price == null) {
            return 0;
        }
        return Math.max(price.amount - getMaterialAmount(player, price.material), 0);
    }

    public static int getMaterialAmount(Player player, Material material) {
        int count = 0;
        for (ItemStack content : player.getInventory().getStorageContents()) {
            //noinspection ConstantConditions
            if (content == null) {
                continue;
            }
            if (content.getType() == material) {
                count += content.getAmount();
            }
        }
        return count;
    }

}
