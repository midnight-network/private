package net.midnightmc.pvp;

import net.midnightmc.core.utils.CommonPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvPCommand extends BukkitCommand {

    public PvPCommand() {
        super("setkit");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!player.hasPermission(CommonPermissions.PVP_SETKIT)) {
            return false;
        }
        PvPManager.getInstance().setKit(player.getInventory());
        player.sendMessage("설정됨.");
        return true;
    }

}
