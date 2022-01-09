package net.midnightmc.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.midnightmc.proxy.MidnightProxy;
import net.midnightmc.proxy.Util;

import java.util.List;

public class SendCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            return;
        }
        RegisteredServer target = MidnightProxy.getServer().getServer(invocation.arguments()[1]).orElse(null);
        if (target == null) {
            invocation.source().sendMessage(Util.getComponent("&c알 수 없는 서버"));
            return;
        }
        if (invocation.arguments()[0].equalsIgnoreCase("staff")) {
            MidnightProxy.getServer().getAllPlayers().stream().filter(player -> player.hasPermission("midnight.staffchat")).forEach(player ->
                    player.createConnectionRequest(target).connect());
        } else if (invocation.arguments()[0].equalsIgnoreCase("all")) {
            MidnightProxy.getServer().getAllPlayers().forEach(player -> player.createConnectionRequest(target).connect());
        } else {
            Player player = Util.getPlayer(invocation.arguments()[0]);
            if (player == null) {
                invocation.source().sendMessage(Util.getComponent("&c알 수 없는 플레이어"));
                return;
            }
            player.createConnectionRequest(target).connect();
            invocation.source().sendMessage(Util.getComponent("&a성공"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            if (invocation.arguments()[0].isEmpty()) {
                return MidnightProxy.getServer().getAllPlayers().stream().map(Player::getUsername).toList();
            }
            return MidnightProxy.getServer().matchPlayer(invocation.arguments()[0]).stream().map(Player::getUsername).toList();
        } else if (invocation.arguments().length == 2) {
            if (invocation.arguments()[1].isEmpty()) {
                return MidnightProxy.getServer().getAllServers().stream().map(server -> server.getServerInfo().getName()).toList();
            }
            return MidnightProxy.getServer().matchServer(invocation.arguments()[1]).stream().map(server -> server.getServerInfo().getName()).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("midnight.command.send");
    }

}
