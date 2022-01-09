package net.midnightmc.bedwars.listeners;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.midnightmc.bedwars.*;
import net.midnightmc.core.game.Game;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class RespawnManager implements Listener {

    @Getter
    private static final RespawnManager instance = new RespawnManager();

    private final HashMap<Player, Tuple<Long, Player>> lastDamage = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> respawnRunnables = new HashMap<>();

    private RespawnManager() {}

    public boolean isRespawning(Player player) {
        return respawnRunnables.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        List<ItemStack> drops = new ArrayList<>(e.getDrops());
        e.getDrops().clear();

        BedwarsGame game = BedwarsManager.getInstance().getGame(e.getEntity().getUniqueId());
        if (game == null) {
            return;
        }
        game.checkGameEnd();
        BedwarsTeam team = game.getTeam(e.getEntity().getUniqueId());
        if (team == null) {
            return;
        }
        // 도구 티어 감소
        game.getShopManager().getAxeTool().decrease(e.getPlayer());
        game.getShopManager().getPickaxeTool().decrease(e.getPlayer());
        game.getShopManager().getShearsTool().decrease(e.getPlayer());
        // 게임 종료 확인
        if (!team.hasBed) {
            game.setSpectator(e.getPlayer());
            game.checkGameEnd();
        }

        //킬 확인
        if (lastDamage.get(e.getEntity()) == null || lastDamage.get(e.getEntity()).x < System.currentTimeMillis() - (1000 * 10)) {
            return;
        }
        Player killer = lastDamage.get(e.getEntity()).y;
        e.setShouldDropExperience(false);
        if (killer != null) {
            HashMap<Material, Integer> resources = new HashMap<>(drops.stream()
                    .filter(itemStack -> itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT
                            || itemStack.getType() == Material.DIAMOND || itemStack.getType() == Material.EMERALD)
                    .collect(Collectors.toMap(ItemStack::getType, ItemStack::getAmount)));
            if (resources.containsKey(Material.IRON_INGOT)) {
                killer.sendMessage(MessageUtil.getComponent("&7+" + resources.get(Material.IRON_INGOT) + " Iron"));
                killer.getInventory().addItem(new ItemStack(Material.IRON_INGOT, resources.get(Material.IRON_INGOT)));
            }
            if (resources.containsKey(Material.GOLD_INGOT)) {
                killer.sendMessage(MessageUtil.getComponent("&6+" + resources.get(Material.GOLD_INGOT) + " Gold"));
                killer.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, resources.get(Material.GOLD_INGOT)));
            }
            if (resources.containsKey(Material.DIAMOND)) {
                killer.sendMessage(MessageUtil.getComponent("&b" + resources.get(Material.DIAMOND) + " Diamonds"));
                killer.getInventory().addItem(new ItemStack(Material.DIAMOND, resources.get(Material.DIAMOND)));
            }
            if (resources.containsKey(Material.EMERALD)) {
                killer.sendMessage(MessageUtil.getComponent("&a" + resources.get(Material.EMERALD) + " Emerlads"));
                killer.getInventory().addItem(new ItemStack(Material.EMERALD, resources.get(Material.EMERALD)));
            }
            if (team.hasBed) {
                game.addKills(killer.getUniqueId());
            } else {
                game.addFinalKills(killer.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof Player attacker) {
            lastDamage.put(victim, new Tuple<>(System.currentTimeMillis(), attacker));
            BedwarsGame game = BedwarsManager.getInstance().getGame(victim.getUniqueId());
            if (game == null) {
                return;
            }
            if (!game.getUUIDs().contains(attacker.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final BedwarsGame game = BedwarsManager.getInstance().getGame(e.getPlayer());
        if (game == null) {
            BedwarsGame worldgame = BedwarsManager.getInstance().getGame(e.getPlayer().getWorld());
            if (worldgame != null) {
                worldgame.setSpectator(e.getPlayer());
            }
            return;
        }
        e.setRespawnLocation(game.getSpawn());
        BedwarsTeam team = game.getTeam(e.getPlayer().getUniqueId());
        if (team == null || !team.hasBed || game.getStatus() != Game.GameStatus.IN_GAME) {
            game.setSpectator(e.getPlayer());
            return;
        }
        e.getPlayer().setAllowFlight(true);
        e.getPlayer().setFlying(true);
        final int[] count = {5};
        ScheduleUtil.sync(() ->
                e.getPlayer().showTitle(Title.title(MessageUtil.parse(e.getPlayer(), "bedwdars-died-title"), Component.empty(),
                        Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO))), 2);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getStatus() != Game.GameStatus.IN_GAME || !e.getPlayer().isOnline()) {
                    cancel();
                    return;
                }
                if (count[0] == 0) {
                    game.tpSpawn(e.getPlayer());
                    cancel();
                    e.getPlayer().showTitle(
                            Title.title(
                                    MessageUtil.parse(e.getPlayer(), "bedwars-respawn"), Component.empty(),
                                    Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))));
                    return;
                }
                e.getPlayer().showTitle(
                        Title.title(
                                MessageUtil.parse(e.getPlayer(), "bedwars-died-title",
                                        Map.of("time", String.valueOf(count[0]))),
                                MessageUtil.parse(e.getPlayer(), "bedwars-died-subtitle",
                                        Map.of("time", String.valueOf(count[0]))),
                                Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(0))));
                count[0]--;
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                respawnRunnables.remove(e.getPlayer().getUniqueId());
                game.updateHidden(e.getPlayer());
                super.cancel();
            }
        };
        respawnRunnables.put(e.getPlayer().getUniqueId(), runnable);
        runnable.runTaskTimer(BedwarsPlugin.getPlugin(), 20L, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        BukkitRunnable runnable = respawnRunnables.remove(e.getPlayer().getUniqueId());
        if (runnable == null) {
            return;
        }
        runnable.cancel();
    }

    public void onGameEnd(BedwarsGame game) {
        game.getUUIDs().forEach(uuid -> {
            BukkitRunnable runnable = respawnRunnables.remove(uuid);
            if (runnable == null) {
                return;
            }
            runnable.cancel();

            Player player = Bukkit.getPlayer(uuid);
            if (!(player == null)) {
                player.teleport(game.getSpawn());
            }
        });
    }

}
