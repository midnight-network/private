package net.midnightmc.pvp;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.coin.CoinEconomy;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

public class PvPManager implements Listener {

    @Getter
    private static final PvPManager instance = new PvPManager();
    private final HashSet<UUID> spectators = new HashSet<>();
    private final World world;
    private final HashMap<UUID, Integer> kills = new HashMap<>();
    private Location spawn;
    private ItemStack[] armor, inv;

    public PvPManager() {
        world = Bukkit.getWorlds().get(0);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setClearWeatherDuration(0);
        loadKit();
        Bukkit.getScheduler().runTaskTimer(PvPPlugin.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> MidnightAPI.getInstance().setBoardLines(player,
                MessageUtil.parseLines(player, "pvp-scoreboard", Map.of(
                        "coins", String.valueOf(CoinEconomy.getInstance().getBalance(player)),
                        "kills", String.valueOf(kills.getOrDefault(player.getUniqueId(), 0))
                )))), 20L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(Component.empty());
        tp(e.getPlayer());
        MidnightAPI.getInstance().setBoardTitle(e.getPlayer(), MessageUtil.getComponent("&6&lPvP"));
        kills.remove(e.getPlayer().getUniqueId());
        ScheduleUtil.sync(() -> e.getPlayer().resetTitle(), 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.empty());
        tp(e.getPlayer());
        kills.remove(e.getPlayer().getUniqueId());
    }

    public void tp(Player player) {
        if (spawn == null) {
            spawn = new Location(world, 0.5, 64, 0.5);
        }
        equipKit(player);
        player.teleport(spawn);
        player.setFireTicks(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setNoDamageTicks(20 * 3);
    }

    public void setKit(PlayerInventory inventory) {
        YamlConfiguration yaml = new YamlConfiguration();
        ItemStack[] items = inventory.getArmorContents();
        for (int i = 0; i < items.length; i++) {
            yaml.set("armor." + i, items[i]);
        }
        items = inventory.getStorageContents();
        for (int i = 0; i < items.length; i++) {
            yaml.set("inv." + i, items[i]);
        }
        String data = yaml.saveToString();
        ScheduleUtil.async(() -> {
            String insert = "INSERT INTO mcnetwork.configuration (name, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value);";
            try (Connection connection = MidnightAPI.getInstance().getHikari().getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(insert)) {
                pstmt.setString(1, "pvp.kit");
                pstmt.setString(2, data);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            MidnightAPI.getInstance().loadConfig();
            loadKit();
        });
    }

    public void loadKit() {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(MidnightAPI.getInstance().getConfiguration().get("pvp.kit"));
            ConfigurationSection section = yaml.getConfigurationSection("armor");
            if (section != null) {
                armor = section.getKeys(false).stream().filter(Objects::nonNull)
                        .map(s -> yaml.getItemStack("armor." + s)).toArray(ItemStack[]::new);
            }
            section = yaml.getConfigurationSection("inv");
            if (section != null) {
                inv = section.getKeys(false).stream().filter(Objects::nonNull)
                        .map(s -> yaml.getItemStack("inv." + s)).toArray(ItemStack[]::new);
            }
        } catch (NullPointerException | InvalidConfigurationException | IllegalArgumentException ex) {
            ex.printStackTrace();
            armor = new ItemStack[]{};
            inv = new ItemStack[]{};
        }
    }

    public void equipKit(Player player) {
        player.getInventory().setContents(inv);
        player.getInventory().setArmorContents(armor);
        player.getInventory().setItemInOffHand(new ItemBuilder(Material.SHIELD).setUnbreakable().build());
        player.setFoodLevel(20);
        player.setHealth(20);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.deathMessage(null);
        e.getDrops().clear();
        kills.put(e.getEntity().getUniqueId(), 0);
        e.getEntity().setBedSpawnLocation(spawn, true);
        spectators.add(e.getEntity().getUniqueId());
        if (e.getEntity().getKiller() != null && e.getEntity().getKiller().isOnline()) {
            e.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
            kills.compute(e.getEntity().getKiller().getUniqueId(), (uuid, integer) -> {
                if (integer == null) {
                    return 1;
                }
                return integer + 1;
            });
            MessageUtil.message(Bukkit.getOnlinePlayers(), "pvp-death-killer",
                    Map.of("player", e.getEntity().getName(), "killer", e.getEntity().getKiller().getName()));
        } else {
            MessageUtil.message(Bukkit.getOnlinePlayers(), "pvp-death",
                    Map.of("player", e.getEntity().getName()));
        }
        ScheduleUtil.sync(() -> {
            if (e.getEntity().isDead()) {
                e.getEntity().spigot().respawn();
            }
        }, 1);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!spectators.contains(e.getPlayer().getUniqueId())) {
            return;
        }
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        final int[] t = {5};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!e.getPlayer().isOnline()) {
                    this.cancel();
                    return;
                }
                if (t[0] > 0) {
                    e.getPlayer().showTitle(Title.title(
                            MessageUtil.parse(e.getPlayer(), "pvp-died-title"),
                            MessageUtil.parse(e.getPlayer(), "pvp-died-subtitle", Map.of("time", String.valueOf(t[0]))),
                            Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(0))
                    ));
                    t[0]--;
                } else {
                    spectators.remove(e.getPlayer().getUniqueId());
                    tp(e.getPlayer());
                    e.getPlayer().clearTitle();
                    e.getPlayer().setFireTicks(0);
                    this.cancel();
                }
            }
        }.runTaskTimer(MidnightCorePlugin.getPlugin(), 1L, 20L);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (spectators.contains(e.getPlayer().getUniqueId()) && e.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        MessageUtil.sendGlobalChat(e.getPlayer(), Component.text("[PvP]"), e);
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(EntityInteractEvent e) {
        if (e.getBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

}
