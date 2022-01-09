package net.midnightmc.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.time.Duration;
import java.util.Map;

public class EventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission(CommonPermissions.WHITECHAT)) {
            e.joinMessage(Component.text("[").color(NamedTextColor.DARK_GRAY).append(Component.text("+").color(NamedTextColor.GREEN))
                    .append(Component.text("] ").color(NamedTextColor.DARK_GRAY)).append(e.getPlayer().displayName()));
        } else {
            e.joinMessage(null);
        }

        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getPlayer().setHealth(20);
        e.getPlayer().setFoodLevel(20);
        e.getPlayer().teleport(LobbyCore.getInstance().getWorld().getSpawnLocation().add(0.5, 0, 0.5));
        LobbyCore.getInstance().hotbar(e.getPlayer());

        MidnightAPI.getInstance().setBoardTitle(e.getPlayer(), MessageUtil.parse(e.getPlayer(), "lobby-scoreboard-title"));

        Bukkit.getScheduler().runTaskLater(LobbyCore.getInstance().getPlugin(), () ->
                e.getPlayer().sendMessage(MessageUtil.parse(
                        MidnightAPI.getInstance().getMessage(MidnightAPI.getInstance().getLang(e.getPlayer().getUniqueId()), "lobby-welcome-message"),
                        Map.of("player", e.getPlayer().getName()))), 1L);
        Bukkit.getScheduler().runTaskLater(LobbyCore.getInstance().getPlugin(), () -> {
            e.getPlayer().showTitle(Title.title(
                    MessageUtil.parse(e.getPlayer(), "lobby-join-title"),
                    MessageUtil.parse(e.getPlayer(), "lobby-join-subtitle"),
                    Title.Times.of(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(2))));
            e.getPlayer().playSound(Sound.sound(Key.key("entity.firework_rocket.twinkle"), Sound.Source.MASTER, 0.5f, 1f));
            e.getPlayer().playSound(Sound.sound(Key.key("entity.firework_rocket.large_blast"), Sound.Source.MASTER, 0.5f, 1f));
            LobbyCore.getInstance().updateScoreBoard(e.getPlayer());
            if (MidnightAPI.getInstance().getLang(e.getPlayer().getUniqueId()) == null) {
                e.getPlayer().chat("/lang");
                e.getPlayer().sendMessage(MessageUtil.getComponent("&a&lselect language!"));
                e.getPlayer().sendMessage(MessageUtil.getComponent("&a&l언어를 선택하세요! &7&l선택하지 않을경우 영어로 표시됩니다."));
            }
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer().hasPermission(CommonPermissions.WHITECHAT)) {
            e.quitMessage(Component.text("[").color(NamedTextColor.DARK_GRAY).append(Component.text("-").color(NamedTextColor.RED))
                    .append(Component.text("] ").color(NamedTextColor.DARK_GRAY)).append(e.getPlayer().displayName()));
        } else {
            e.quitMessage(null);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player && e.getEntity().getWorld().getName().equals(MidnightAPI.getInstance().getConfiguration().get("lobby.world"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) {
            return;
        }
        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            e.setDamage(0);
            victim.setHealth(0);
        }
        Entity attacker = null;
        if (e instanceof EntityDamageByEntityEvent) {
            attacker = ((EntityDamageByEntityEvent) e).getDamager();
        }
        if (victim.getWorld().getName().equals(MidnightAPI.getInstance().getConfiguration().get("lobby.world"))) {
            if (attacker == null || !(attacker.hasPermission(CommonPermissions.LOBBY_PVP))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getEntity().getWorld().getName().equals(MidnightAPI.getInstance().getConfiguration().get("lobby.world"))) {
            e.getEntity().setBedSpawnLocation(e.getEntity().getWorld().getSpawnLocation().add(0.5, 0, 0.5), true);
            Bukkit.getScheduler().runTaskLater(LobbyCore.getInstance().getPlugin(), () -> e.getEntity().spigot().respawn(), 1L);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (e.getWorld().getName().equals(MidnightAPI.getInstance().getConfiguration().get("lobby.world"))) {
            if (e.toWeatherState()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTp(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (e.getPlayer().getWorld().getName().equals(MidnightAPI.getInstance().getConfiguration().get("lobby.world"))) {
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            LobbyCore.getInstance().hotbar(e.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        MessageUtil.sendGlobalChat(e.getPlayer(), Component.text("[Lobby]"), e);
        e.setCancelled(true);
    }


}
