package net.midnightmc.core.commands;

import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.gui.GUI;
import net.midnightmc.core.gui.GUIItem;
import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LanguageCommand extends BukkitCommand {

    public final GUI gui = new GUI("&b&lLanguage 언어", 3)
            .setItem(10, new GUIItem(ItemBuilder.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMxYmU1ZjEyZjQ1ZTQxM2VkYTU2ZjNkZTk0ZTA4ZDkwZWRlOGUzMzljN2IxZThmMzI3OTczOTBlOWE1ZiJ9fX0=")
                    .setName("&f&l한국어").build())
                    .setExecute(player -> MidnightAPI.getInstance().setLang(player.getUniqueId(), "KO")))
            .setItem(12, new GUIItem(ItemBuilder.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNiYzMyY2IyNGQ1N2ZjZGMwMzFlODUxMjM1ZGEyZGFhZDNlMTkxNGI4NzA0M2JkMDEyNjMzZTZmMzJjNyJ9fX0=")
                    .setName("&f&lEnglish").build())
                    .setExecute(player -> MidnightAPI.getInstance().setLang(player.getUniqueId(), "EN"))
            );

    public LanguageCommand() {
        super("language");
        setAliases(Arrays.asList("lang", "언어"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length < 1) {
            gui.open(player);
            return true;
        }
        switch (args[0].toUpperCase()) {
            case "KO" -> MidnightAPI.getInstance().setLang(player.getUniqueId(), "KO");
            case "EN" -> MidnightAPI.getInstance().setLang(player.getUniqueId(), "EN");
            case "LOAD" -> {
                if (player.hasPermission(CommonPermissions.LANG_RELOAD)) {
                    player.sendMessage(MessageUtil.getComponent("&f언어 로드중"));
                    Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), () -> {
                        MidnightAPI.getInstance().loadLang();
                        player.sendMessage(MessageUtil.getComponent("&a언어 로드 성공"));
                    });
                    return true;
                }
            }
            default -> {
                player.sendMessage(MessageUtil.getComponent("&c올바르지 않은 언어입니다. &f가능한 언어들 : KO, EN"));
                return false;
            }
        }
        return true;
    }

}
