package net.midnightmc.proxy.discord;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordVerifyCommand extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getName().equalsIgnoreCase("verify")) {
            event.reply("준비중인 기능입니다. Verify is currently not supported").setEphemeral(true).queue();
        }
    }

}
