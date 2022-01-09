package net.midnightmc.bedwars.shop;

import net.midnightmc.bedwars.BedwarsGame;
import net.midnightmc.bedwars.BedwarsManager;
import net.midnightmc.bedwars.BedwarsTeam;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ShopArmorItem extends ShopItem {

    private static final HashMap<UUID, Integer> tiers = new HashMap<>();
    private final int tier;

    public ShopArmorItem(String game, String name, int tier) {
        super(game, name);
        this.tier = tier;
    }

    public static void reset(Player player) {
        tiers.remove(player.getUniqueId());
    }

    public static String getArmorType(Player player) {
        return switch (tiers.getOrDefault(player.getUniqueId(), 0)) {
            case 1 -> "CHAINMAIL";
            case 2 -> "IRON";
            case 3 -> "DIAMOND";
            default -> "LEATHER";
        };
    }

    @Override
    protected boolean canBuy(Player player) {
        if (tiers.getOrDefault(player.getUniqueId(), 0) >= tier) {
            player.sendMessage(MessageUtil.parse(player, "bedwars-upgrade-fail"));
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        ItemStack item = super.getItemStack(player);
        BedwarsTeam team = BedwarsManager.getInstance().getBedwarsTeam(player.getUniqueId());
        if (team == null) {
            return item;
        }
        return new ItemBuilder(item).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protection).build();
    }

    @Override
    protected void onPurchase(Player player) {
        tiers.put(player.getUniqueId(), tier);
        BedwarsGame.equipArmor(BedwarsManager.getInstance().getBedwarsTeam(player.getUniqueId()), player);
    }

}
