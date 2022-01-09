package net.midnightmc.proxy.discord;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.midnightmc.proxy.MidnightProxy;
import net.midnightmc.proxy.StaffChat;

import javax.security.auth.login.LoginException;
import java.util.Collections;

public class DiscordManager {

    private static final String DISCORD_SERVER = "768045296134782996";
    private static final String LOG_CHANNEL = "778953961074982912";
    private static final DiscordManager instance = new DiscordManager();
    public static JDA bot;

    private DiscordManager() {
    }

    public static DiscordManager getInstance() {
        return instance;
    }

    public void init() {
        try {
            bot = JDABuilder.createDefault("NzcxMjEzNDY0MTMwODEzOTYz.X5o2gg.OBHHAAexJsB50YFBH3aOJ1uRW1Q")
                    .addEventListeners(StaffChat.getInstance()).build();
        } catch (LoginException ex) {
            ex.printStackTrace();
        }
        bot.getPresence().setStatus(OnlineStatus.ONLINE);
        bot.getPresence().setActivity(Activity.playing("Loading.. 연결중.."));
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MidnightProxy.getLogger().info("Discord Bot Connected");
        bot.updateCommands().addCommands(Collections.emptyList()).queue();
        bot.getPresence().setActivity(Activity.playing("Midnight Network"));
        bot.getGuilds().stream().filter(guild -> !guild.getId().equals(DISCORD_SERVER)).forEach(guild -> guild.leave().queue());
        Guild guild = bot.getGuildById(DISCORD_SERVER);
        if (guild == null) {
            return;
        }
        guild.updateCommands().addCommands(
                new CommandData("verify", "Verify your discord/minecraft account 디스코드, 마인크래프트 계정 연동하기")
                        .addOption(OptionType.STRING, "username", "Minecraft nickname 마인크래프트 닉네임", false)
        ).queue();
        bot.addEventListener(new DiscordVerifyCommand());
    }

    public void sendMsg(String msg, String channelId) {
        MessageChannel ch = bot.getTextChannelById(channelId);
        sendMsg(msg, ch);
    }

    public void sendMsg(String msg, MessageChannel channel) {
        if (channel == null) return;
        channel.sendMessage(MarkdownSanitizer.escape(msg)).queue();
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent e) {
        if (e.getPreviousServer().isEmpty()) return;
        sendMsg(e.getPlayer().getUsername() + " : " + e.getPreviousServer().get().getServerInfo().getName()
                + "->" + e.getServer().getServerInfo().getName(), LOG_CHANNEL);
    }

    @Subscribe
    public void onJoin(PostLoginEvent e) {
        sendMsg("[+]" + e.getPlayer().getUsername(), LOG_CHANNEL);
    }

    @Subscribe
    public void onQuit(DisconnectEvent e) {
        sendMsg("[-]" + e.getPlayer().getUsername(), LOG_CHANNEL);
    }

}
