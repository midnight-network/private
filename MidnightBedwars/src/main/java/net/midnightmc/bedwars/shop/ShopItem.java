package net.midnightmc.bedwars.shop;

import dev.morphia.query.experimental.filters.Filters;
import lombok.Getter;
import net.midnightmc.bedwars.BedwarsManager;
import net.midnightmc.bedwars.BedwarsTeam;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.gui.GUIItem;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShopItem extends GUIItem {

    public static final ItemStack INVALID_ITEM = new ItemStack(Material.BARRIER);

    private final ItemStack item;
    @Getter
    private final BedwarsItem.Price price;
    @Getter
    private boolean isValid = false;

    public ShopItem(@NotNull String game, @NotNull String name) {
        this(game, name, false);
    }

    public ShopItem(@NotNull String game, @NotNull String name, boolean ignoreInvalid) {
        super(INVALID_ITEM);
        BedwarsItem bwteam = MidnightAPI.getInstance().getMorphia().find(BedwarsItem.class)
                .filter(Filters.eq("name", name)).first();
        if (bwteam == null || !bwteam.prices.containsKey(game)) {
            this.item = INVALID_ITEM;
            this.price = new BedwarsItem.Price(Material.AIR, 0);
            if (!ignoreInvalid) {
                return;
            }
        } else {
            this.item = bwteam.getItem();
            this.price = bwteam.prices.get(game);
        }
        this.isValid = true;
        setExecute(player -> {
            int require = BedwarsShopUtil.requiredAmount(player, price);
            if (!canBuy(player)) {
                return;
            }
            //살 수 있다면
            if (require == 0) {
                player.getInventory().removeItem(new ItemStack(price.material, price.amount));
                onPurchase(player);
                MessageUtil.message(Collections.singleton(player), "bedwars-shop-success", Map.of("item", name.replaceAll("_", " ").toUpperCase()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            } else {
                MessageUtil.message(Collections.singleton(player), "bedwars-shop-fail", Map.of("amount", String.valueOf(require)));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        }, false);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean canBuy(Player player) {
        return true;
    }

    /**
     * Returns the item that will be showed in shop gui
     *
     * @param player the player
     * @return the item that will be showed in shop gui
     */
    @Override
    public ItemStack getItemStack(Player player) {
        ItemStack itemStack = getItem(player);
        String material = switch (price.material) {
            case IRON_INGOT -> "Iron";
            case GOLD_INGOT -> "Gold";
            case DIAMOND -> "Diamond";
            case EMERALD -> "Emerald";
            default -> "Unknown";
        };
        itemStack.lore(List.of(MessageUtil.parseLines(player, "bedwars-shop-price", Map.of("price", price.amount + " " + material))));
        return itemStack;
    }

    /**
     * Returns the item that will be given to the player
     *
     * @param player the player
     * @return the item that will be given to the player
     */
    public ItemStack getItem(Player player) {
        if (Tag.WOOL.isTagged(item.getType())) {
            BedwarsTeam team = BedwarsManager.getInstance().getBedwarsTeam(player.getUniqueId());
            if (team == null) {
                return item.clone();
            }
            return ItemBuilder.getColoredWool(team.getDyeColor()).setAmount(item.getAmount()).build();
        }
        return item.clone();
    }

    protected void onPurchase(Player player) {
        if (item.getType().name().endsWith("_SWORD")) {
            BedwarsTeam team = BedwarsManager.getInstance().getBedwarsTeam(player.getUniqueId());
            if (team == null) {
                return;
            }
            ItemStack sword = new ItemBuilder(getItem(player)).addEnchant(Enchantment.DAMAGE_ALL, team.sharpness).build();
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (itemStack != null && itemStack.getType().name().endsWith("_SWORD")) {
                    player.getInventory().setItem(i, sword);
                    return;
                }
            }
            player.getInventory().addItem(sword);
            return;
        }
        player.getInventory().addItem(getItem(player));
    }

}
