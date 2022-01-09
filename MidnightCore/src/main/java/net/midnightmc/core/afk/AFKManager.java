package net.midnightmc.core.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.midnightmc.core.MidnightCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class AFKManager extends Command implements Listener {

    @Getter
    private static final AFKManager instace = new AFKManager();

    private final HashMap<UUID, Integer> afkplayers = new HashMap<>();
    private final HashMap<UUID, Location> afkdata = new HashMap<>();

    private AFKManager() {
        super("afk");
    }

    public void init() {
        Bukkit.getScheduler().runTaskTimer(MidnightCorePlugin.getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                afkdata.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
                Location previous = afkdata.get(player.getUniqueId());
                if (previous.equals(player.getLocation()) || ((player.isInWater() || player.isInLava())
                        && previous.getYaw() == player.getLocation().getYaw() && previous.getPitch() == player.getLocation().getPitch())) {
                    addTime(player);
                } else {
                    reset(player);
                }
                afkdata.put(player.getUniqueId(), player.getLocation().clone());
            }
        }, 20L, 20L);
    }

    public int getTime(Player player) {
        return afkplayers.getOrDefault(player.getUniqueId(), 0);
    }

    private void addTime(Player player) {
        afkplayers.compute(player.getUniqueId(), (uuid, integer) -> {
            if (integer == null) {
                return 1;
            } else {
                return integer + 1;
            }
        });
    }

    public void reset(Player player) {
        afkplayers.put(player.getUniqueId(), 0);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            player.sendMessage(afkplayers.get(player.getUniqueId()).toString());
        }
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        afkplayers.remove(e.getPlayer().getUniqueId());
        afkdata.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        reset(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        reset(e.getPlayer());
    }

}
