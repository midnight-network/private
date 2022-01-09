package net.midnightmc.bedwars.shop;

import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShopToolsItem extends ShopItem {

    private final HashMap<UUID, Integer> tiers = new HashMap<>();
    private final ArrayList<ShopItem> items = new ArrayList<>();

    public ShopToolsItem(String game, String name) {
        super(game, name, true);
        items.add(0, null);
        for (int i = 1; ; i++) {
            ShopItem item = new ShopItem(game, name + "_" + i);
            if (!item.isValid()) {
                break;
            }
            items.add(i, item);
        }
        setExecute(player -> {
            if (!canBuy(player)) {
                return;
            }
            BedwarsItem.Price price = items.get(tiers.getOrDefault(player.getUniqueId(), 0) + 1).getPrice();
            int require = BedwarsShopUtil.requiredAmount(player, price);
            //살 수 있다면
            if (require == 0) {
                player.getInventory().removeItem(new ItemStack(price.material, price.amount));
                tiers.compute(player.getUniqueId(), (uuid, tier) -> {
                    if (tier == null) {
                        return 1;
                    }
                    return tier + 1;
                });
                onPurchase(player);
                MessageUtil.message(Collections.singleton(player), "bedwars-shop-success", Map.of("item",
                        name.replaceAll("_", " ").toUpperCase() + " " + tiers.getOrDefault(player.getUniqueId(), 0)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            } else {
                MessageUtil.message(Collections.singleton(player), "bedwars-shop-fail", Map.of("amount", String.valueOf(require)));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        }, false);
    }

    public int getMaxTier() {
        return items.size() - 1;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        int tier = tiers.getOrDefault(player.getUniqueId(), 0);
        if (tier >= getMaxTier()) {
            return new ItemStack(Material.BARRIER);
        }
        return items.get(tier + 1).getItemStack(player);
    }

    @Override
    public @NotNull ItemStack getItem(Player player) {
        int tier = tiers.getOrDefault(player.getUniqueId(), 0);
        if (tier <= 0) {
            return new ItemStack(Material.AIR);
        }
        return items.get(tier).getItem(player);
    }

    @Override
    protected boolean canBuy(Player player) {
        if (tiers.getOrDefault(player.getUniqueId(), 0) >= getMaxTier()) {
            tiers.put(player.getUniqueId(), getMaxTier());
            return false;
        }
        return true;
    }

    @Override
    protected void onPurchase(Player player) {
        items.stream().filter(Objects::nonNull).forEach(shopItem -> player.getInventory().remove(shopItem.getItem(player).getType()));
        player.getInventory().addItem(getItem(player));
    }

    public void reset(Player player) {
        tiers.remove(player.getUniqueId());
    }

    public void decrease(Player player) {
        if (!tiers.containsKey(player.getUniqueId())) {
            return;
        }
        int tier = tiers.get(player.getUniqueId());
        if (tier <= 0) {
            tiers.remove(player.getUniqueId());
            return;
        }
        if (tier > 1) {
            tiers.put(player.getUniqueId(), tier - 1);
        }
    }

}
