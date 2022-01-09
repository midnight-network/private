package net.midnightmc.core.commands;

import net.midnightmc.core.utils.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class URLPaste extends BukkitCommand {

    public URLPaste() {
        super("urlpaste");
        setPermission(CommonPermissions.URLPASTE);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(MessageUtil.getComponent("사용법: /paste <url>"));
            return false;
        }
        ScheduleUtil.async(() -> {
            player.sendMessage(MessageUtil.getComponent("&a다운로드 중.."));
            File file = new File(player.getWorld().getWorldFolder(), "cache");
            FileUtils.deleteQuietly(file);
            if (!IOUtil.download(args[0], file)) {
                player.sendMessage(MessageUtil.getComponent("&c오류 발생"));
                return;
            }
            player.sendMessage(MessageUtil.getComponent("&a다운로드 성공\n&a붙여넣기중.."));
            if (FaweUtil.paste(player.getWorld(), file, player.getLocation(), true) == 0) {
                player.sendMessage(MessageUtil.getComponent("&c붙여넣기 실패"));
            } else {
                player.sendMessage(MessageUtil.getComponent("&a붙여넣기 성공!"));
            }
        });
        return true;
    }

}
