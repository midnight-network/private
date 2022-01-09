package net.midnightmc.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.midnightmc.proxy.Util;

public class DiscordCommand implements SimpleCommand {

    private final Component component = Util.getComponent("&9&lDiscord : &r&ahttps://MidnightMC.net/discord")
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click!").color(NamedTextColor.YELLOW)))
            .clickEvent(ClickEvent.openUrl("https://MidnightMC.net/discord"));

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(component);
    }

}
