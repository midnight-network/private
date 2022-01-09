package net.midnightmc.proxy;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

public class JoinLeaveMessage {

    @Subscribe
    public void onJoin(PostLoginEvent e) {
        MidnightProxy.getServer().getConsoleCommandSource().sendMessage(Util.getComponent("&8[&a+&8]&7 " + e.getPlayer().getUsername()));
        String msg = "&ajoin: &f" + e.getPlayer().getUsername();
        MidnightProxy.getServer().getAllPlayers().stream().filter(player -> player.hasPermission(StaffChat.getPermission())).forEach(player ->
                Util.sendMessage(player, msg));
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        MidnightProxy.getServer().getConsoleCommandSource().sendMessage(Util.getComponent("&8[&4-&8]&7 " + e.getPlayer().getUsername()));
        String msg = "&cleave: &f" + e.getPlayer().getUsername();
        MidnightProxy.getServer().getAllPlayers().stream().filter(player -> player.hasPermission(StaffChat.getPermission())).forEach(player ->
                Util.sendMessage(player, msg));
    }

}
