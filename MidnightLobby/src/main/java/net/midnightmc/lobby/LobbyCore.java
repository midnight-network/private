package net.midnightmc.lobby;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.gui.GUI;
import net.midnightmc.core.gui.GUIItem;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.MidnightUtil;
import net.midnightmc.lobby.commands.FlyCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class LobbyCore {

    private static LobbyCore instance;
    private static Plugin plugin;
    private final World world;

    @Getter
    private final GUI gamegui = new GUI("&5Game Selector", 4)
            .setItem(new GUIItem(new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).guiMode().setName("").build()),
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 27, 28, 29, 30, 31, 32, 33, 34, 35)
            .setItem(new GUIItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE).guiMode().setName("").build()),
                    9, 11, 12, 14, 15, 17, 18, 20, 21, 23, 24, 26)

            .setItem(10, new GUIItem(new ItemBuilder(Material.DIAMOND_SWORD).guiMode().setName("&5&lPvP").build()).setExecute(player ->
                    MidnightUtil.sendPlayer(player, "PvP-1")))
            .setItem(13, new GUIItem(new ItemBuilder(Material.RED_BED).guiMode().setName("&b&lBedwars").build()))
            .setItem(16, new GUIItem(new ItemBuilder(Material.GOLDEN_AXE).guiMode().addEnchant(Enchantment.DIG_SPEED, 1)
                    .setName("&4&lMurder Mystery").build()))
            .setItem(19, new GUIItem(new ItemBuilder(Material.ENDER_EYE).guiMode().setName("&3&lSkywars").build()))
            .setItem(22, new GUIItem(new ItemBuilder(Material.GOLDEN_APPLE).guiMode().setName("&6&lUHC").build()))
            .setItem(25, new GUIItem(new ItemBuilder(Material.GRASS_BLOCK).guiMode().setName("&a&lSurvival").build()));

    public LobbyCore(Plugin plugin) {
        LobbyCore.instance = this;
        LobbyCore.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new EventListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new HotbarListener(), plugin);

        world = Bukkit.getWorld(MidnightAPI.getInstance().getConfiguration().get("lobby.world"));
        if (world == null) {
            throw new NullPointerException("world is null");
        }
        world.setFullTime(6000);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        Bukkit.getServer().getCommandMap().register("lobby", new FlyCommand());
        Bukkit.clearRecipes();
        registerSchedule();


    }

    public static LobbyCore getInstance() {
        return instance;
    }

    public void onDisable() {

    }

    public Plugin getPlugin() {
        return plugin;
    }

    public World getWorld() {
        return world;
    }

    private void registerSchedule() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreBoard(player);
            }
        }, 0, 20L * 2);
    }

    public void updateScoreBoard(Player player) {
        if (MidnightUtil.isVanished(player)) {
            return;
        }
        MidnightAPI.getInstance().setBoardLines(player,
                MidnightAPI.getInstance().getMessage(MidnightAPI.getInstance().getLang(player.getUniqueId()), "lobby-scoreboard").lines()
                        .map(s -> MessageUtil.parse(s, Map.of("player_name", player.getName(),
                                "prefix", MessageUtil.getParsedString(MidnightAPI.getInstance().getPrefix(player)),
                                "coins", String.valueOf(PlayerData.get(player.getUniqueId()).getCoins())))
                        ).toArray(Component[]::new)
        );
    }

    public void hotbar(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemBuilder(Material.CLOCK).guiMode().setName("&5&lGame Selector").build());
        player.getInventory().setItem(4, new ItemBuilder(Material.EMERALD).guiMode().setName("&a&lSHOP").build());
    }

}
