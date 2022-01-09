package net.midnightmc.core.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import fr.mrmicky.fastboard.FastBoard;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.midnightmc.core.BasicChatFormat;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.util.Arrays;
import java.util.Collection;

public class EventListener implements Listener {

    @EventHandler
    public void onJoinAsync(AsyncPlayerPreLoginEvent e) {
        if (!PlayerData.load(e.getUniqueId(), e.getName())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageUtil.getComponent("시스템 내부 오류.\n관리자에게 문의하세요."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        String prefix = MidnightAPI.getInstance().getPrefix(e.getPlayer());
        e.getPlayer().displayName(MessageUtil.getComponent(prefix + (prefix.equals("") ? "" : " ") + "&f" + e.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        FastBoard board = MidnightAPI.getInstance().getBoards().remove(e.getPlayer().getUniqueId());
        if (board != null) {
            board.delete();
        }
        PlayerData.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        e.renderer(BasicChatFormat.getInstance());
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        Collection<String> list = e.getCommands();
        list.removeIf(s -> s.split(":").length >= 2);
        list.addAll(Arrays.asList("Midnight:help", "MidnightCore:help"));
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (e.isCommand()) {
            e.getCompletions().forEach(s -> {
                if (s.split(" ")[0].contains(":")) {
                    e.getCompletions().remove(s);
                }
            });
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
        e.getWorld().setKeepSpawnInMemory(false);
    }

}
