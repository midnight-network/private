package net.midnightmc.lobby.commands;

import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand extends BukkitCommand {

    public FlyCommand() {
        super("fly");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission(CommonPermissions.LOBBY_FLY)) {
            return false;
        }
        if (sender instanceof Player player) {
            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage(MessageUtil.getComponent("&b플라이 &c비활성"));
            } else {
                player.setAllowFlight(true);
                player.sendMessage(MessageUtil.getComponent("&b플라이 &a활성"));
            }
            return true;
        }
        return false;
    }

}
