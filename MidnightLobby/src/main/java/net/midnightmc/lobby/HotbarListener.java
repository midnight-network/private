package net.midnightmc.lobby;

import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarListener implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.getWorld().equals(LobbyCore.getInstance().getWorld())) {
            return;
        }
        if (player.hasPermission(CommonPermissions.LOBBY_HOTBAR)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getPlayer().getWorld().equals(LobbyCore.getInstance().getWorld())) {
            return;
        }
        e.setCancelled(true);
        switch (e.getMaterial()) {
            case CLOCK -> {
                LobbyCore.getInstance().getGamegui().open(e.getPlayer());
            }
            case EMERALD -> {
                e.getPlayer().sendMessage(MessageUtil.getComponent("&c준비중.."));
            }
        }
    }

}
