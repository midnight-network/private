package net.midnightmc.bedwars.shop;

import lombok.Getter;
import net.midnightmc.bedwars.BedwarsGame;
import net.midnightmc.core.gui.GUI;
import net.midnightmc.core.gui.GUIItem;
import net.midnightmc.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopManager {

    private final GUI ITEM_BLOCKS;
    private final GUI ITEM_WEAPON;
    private final GUI ITEM_ARMOR;
    private final GUI ITEM_TOOLS;
    private final GUI ITEM_POTIONS;
    private final GUI ITEM_OTHERS;
    private final GUI UPGRADE;
    @Getter
    private final ShopToolsItem axeTool, pickaxeTool, shearsTool;

    public ShopManager(BedwarsGame game) {
        axeTool = new ShopToolsItem(game.getGame(), "tools_axe");
        pickaxeTool = new ShopToolsItem(game.getGame(), "tools_pickaxe");
        shearsTool = new ShopToolsItem(game.getGame(), "tools_shears");
        ITEM_BLOCKS = createItemShop("Blocks")
                .setItem(12, new ShopItem(game.getGame(), "wool"))
                .setItem(13, new ShopItem(game.getGame(), "planks"))
                .setItem(14, new ShopItem(game.getGame(), "cobblestone"))
                .setItem(21, new ShopItem(game.getGame(), "glass"))
                .setItem(22, new ShopItem(game.getGame(), "obsidian"))
                .setItem(23, new ShopItem(game.getGame(), "sandstone"));
        ITEM_WEAPON = createItemShop("Weapon")
                .setItem(12, new ShopItem(game.getGame(), "stone_sword"))
                .setItem(13, new ShopItem(game.getGame(), "iron_sword"))
                .setItem(14, new ShopItem(game.getGame(), "diamond_sword"))
                .setItem(15, new ShopItem(game.getGame(), "knockstick"))
                .setItem(30, new ShopItem(game.getGame(), "bow"))
                .setItem(31, new ShopItem(game.getGame(), "knockbow"))
                .setItem(32, new ShopItem(game.getGame(), "powerbow"))
                .setItem(33, new ShopItem(game.getGame(), "arrows"));
        ITEM_ARMOR = createItemShop("Armor")
                .setItem(21, new ShopArmorItem(game.getGame(), "chainmail_armor", 1))
                .setItem(22, new ShopArmorItem(game.getGame(), "iron_armor", 2))
                .setItem(23, new ShopArmorItem(game.getGame(), "diamond_armor", 3));
        ITEM_TOOLS = createItemShop("Tools")
                .setItem(12, axeTool)
                .setItem(13, pickaxeTool)
                .setItem(14, shearsTool)
                .setItem(15, new ShopItem(game.getGame(), "fishing_rod"));
        ITEM_POTIONS = createItemShop("Potions")
                .setItem(21, new ShopItem(game.getGame(), "invisibility_potion"))
                .setItem(22, new ShopItem(game.getGame(), "jumpboost_potion"))
                .setItem(23, new ShopItem(game.getGame(), "healing_potion"));
        ITEM_OTHERS = createItemShop("Others")
                .setItem(21, new ShopItem(game.getGame(), "waterbukkit"))
                .setItem(22, new ShopItem(game.getGame(), "goldenapple"))
                .setItem(23, new ShopItem(game.getGame(), "skeleton_spawn_egg"))
                .setItem(24, new ShopItem(game.getGame(), "tnt"))
                .setItem(25, new ShopItem(game.getGame(), "fireball"))
                .setItem(32, new ShopItem(game.getGame(), "ender_pearl"));
        UPGRADE = new GUI("&b&lUpgrade &6&lShop", 3)
                .setItem(10, new ShopItem(game.getGame(), "upgrade-sharpness"))
                .setItem(11, new ShopItem(game.getGame(), "upgrade-protection"));
    }

    private GUI createItemShop(String category) {
        return new GUI("&6&lItem Shop &7-> &d" + category, 6)
                .setItem(new GUIItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE).guiMode().setName("").build()), 1, 10, 19, 28, 37, 46)
                .setItem(0, new GUIItem(new ItemBuilder(Material.OAK_PLANKS).setName("&fBlocks").build())
                        .setExecute(player -> openItemShop(player, "blocks"), false))
                .setItem(9, new GUIItem(new ItemBuilder(Material.DIAMOND_SWORD).setName("&fWeapon").build())
                        .setExecute(player -> openItemShop(player, "weapon"), false))
                .setItem(18, new GUIItem(new ItemBuilder(Material.IRON_CHESTPLATE).setName("&fArmor").build())
                        .setExecute(player -> openItemShop(player, "armor"), false))
                .setItem(27, new GUIItem(new ItemBuilder(Material.IRON_PICKAXE).setName("&fTools").build())
                        .setExecute(player -> openItemShop(player, "tools"), false))
                .setItem(36, new GUIItem(new ItemBuilder(Material.POTION).setName("&fPotions").build())
                        .setExecute(player -> openItemShop(player, "potions"), false))
                .setItem(45, new GUIItem(new ItemBuilder(Material.GOLDEN_APPLE).setName("&fOthers").build())
                        .setExecute(player -> openItemShop(player, "others"), false));
    }

    public void openItemShop(Player player) {
        openItemShop(player, "blocks");
    }

    public void openItemShop(Player player, String category) {
        switch (category.toLowerCase()) {
            case "blocks" -> ITEM_BLOCKS.open(player);
            case "weapon" -> ITEM_WEAPON.open(player);
            case "armor" -> ITEM_ARMOR.open(player);
            case "tools" -> ITEM_TOOLS.open(player);
            case "potions" -> ITEM_POTIONS.open(player);
            case "others" -> ITEM_OTHERS.open(player);
        }
    }

    public void openUpgradeShop(Player player) {
        UPGRADE.open(player);
    }

}
