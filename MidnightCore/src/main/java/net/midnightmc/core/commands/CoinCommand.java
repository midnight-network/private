package net.midnightmc.core.commands;

import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.coin.CoinManager;
import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CoinCommand extends BukkitCommand {

    public CoinCommand() {
        super("coin");
        setAliases(Arrays.asList("money", "eco"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        execute(sender, List.of(args));
        return true;
    }

    public void execute(CommandSender sender, List<String> args) {
        if (sender.hasPermission(CommonPermissions.COINS) && args.size() > 0) {
            admin(sender, args);
            return;
        }
        if (sender instanceof Player player) {
            player.sendMessage(MessageUtil.getComponent("&6Coins : " + PlayerData.get(player.getUniqueId()).getCoins()));
        }
    }

    public void admin(CommandSender sender, final List<String> args) {
        if (args.size() < 3) {
            sender.sendMessage(MessageUtil.getComponent("&c잘못된 명령어"));
            return;
        }
        if (!args.get(0).equalsIgnoreCase("set") && !args.get(0).equalsIgnoreCase("add") && !args.get(0).equalsIgnoreCase("subtract")) {
            sender.sendMessage(MessageUtil.getComponent("&c알 수 없는 명령어: &f" + args.get(0)));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), () -> {
            UUID uuid = UUIDUtils.getUUIDAsync(args.get(1));
            if (uuid == null) {
                sender.sendMessage(MessageUtil.getComponent("&c알 수 없는 플레이어"));
                return;
            }
            int amount;
            try {
                amount = Integer.parseInt(args.get(2));
            } catch (NumberFormatException ex) {
                sender.sendMessage(MessageUtil.getComponent("&c숫자를 제대로 입력하세요."));
                return;
            }
            if (amount < 0) {
                sender.sendMessage(MessageUtil.getComponent("&c아니 어떻게 코인을 마이너스로 설정하냐고"));
                return;
            }
            if (CoinManager.getInstance().apply(uuid, args.get(0), amount)) {
                sender.sendMessage(MessageUtil.getComponent("&a적용됨"));
            } else if (CoinManager.getInstance().query(uuid, args.get(0), amount)) {
                sender.sendMessage(MessageUtil.getComponent("&a적용됨 (오프라인 플레이어)"));
            } else {
                sender.sendMessage(MessageUtil.getComponent("&c오류 발생! 개발자(리자)에게 문의하세요!"));
            }
        });
    }

}
