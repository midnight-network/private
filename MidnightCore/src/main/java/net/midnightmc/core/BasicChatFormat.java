package net.midnightmc.core;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.midnightmc.core.utils.CommonPermissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BasicChatFormat implements ChatRenderer {

    private static final BasicChatFormat instance = new BasicChatFormat();

    private BasicChatFormat() {
    }

    public static BasicChatFormat getInstance() {
        return instance;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        if (source.hasPermission(CommonPermissions.WHITECHAT)) {
            return sourceDisplayName.append(Component.text(" : ")).color(NamedTextColor.WHITE)
                    .append(message).color(NamedTextColor.WHITE);
        } else {
            return sourceDisplayName.color(NamedTextColor.GRAY).append(Component.text(" : ")).color(NamedTextColor.GRAY)
                    .append(message).color(NamedTextColor.GRAY);
        }
    }

}
