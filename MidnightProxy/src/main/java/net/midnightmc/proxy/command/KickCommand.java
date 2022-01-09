package net.midnightmc.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.midnightmc.proxy.MidnightProxy;
import net.midnightmc.proxy.Util;

import java.util.Arrays;
import java.util.List;

public class KickCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        Player target = null;
        if (invocation.arguments().length >= 1) {
            target = Util.getPlayer(invocation.arguments()[0]);
        }
        if (target == null) {
            invocation.source().sendMessage(Util.getComponent("&c알 수 없는 플레이어"));
            return;
        }

        String reason = null;
        if (invocation.arguments().length >= 2) {
            reason = String.join(" ", Arrays.copyOfRange(invocation.arguments(), 1, invocation.arguments().length));
        }
        if (reason == null) {
            target.disconnect(Component.empty());
        } else {
            target.disconnect(Component.text(reason));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return MidnightProxy.getServer().matchPlayer(invocation.arguments()[0]).stream().map(Player::getUsername).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("midnight.kick");
    }

}
