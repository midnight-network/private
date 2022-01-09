package net.midnightmc.proxy;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.kyori.adventure.text.Component;
import net.midnightmc.proxy.discord.DiscordManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class StaffChat implements EventListener {

    @Getter
    private static final StaffChat instance = new StaffChat();
    private static final String STAFF_CHANNEL = "795591700428750868";
    @Getter
    private static final String Permission = "midnight.staffchat";
    private static final Component PREFIX = Util.getComponent("&9STAFF &8> &f");
    private final HashSet<UUID> staffChatMode = new HashSet<>();

    private StaffChat() {
    }

    public boolean isStaffChatMode(Player player) {
        return staffChatMode.contains(player.getUniqueId());
    }

    public void setStaffChatMode(Player player, boolean mode) {
        if (mode) {
            staffChatMode.add(player.getUniqueId());
        } else {
            staffChatMode.remove(player.getUniqueId());
        }
    }

    public void sendStaffChat(String msg) {
        final Component message = PREFIX.append(Util.getComponent(msg));
        MidnightProxy.getServer().getConsoleCommandSource().sendMessage(message);
        for (Player p : MidnightProxy.getServer().getAllPlayers()) {
            if (p.hasPermission(getPermission())) {
                p.sendMessage(message);
            }
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent e) {
        if (e.getMessage().startsWith("/")) {
            return;
        }
        Player player = e.getPlayer();
        if (staffChatMode.contains(player.getUniqueId())) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
            DiscordManager.getInstance().sendMsg(player.getUsername() + " : " + e.getMessage(), STAFF_CHANNEL);
            sendStaffChat("&e" + player.getUsername() + " : &f" + e.getMessage());
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildMessageReceivedEvent e) {
            if (e.getGuild().getId().equals("768045296134782996")
                    && e.getChannel().getId().equals("795591700428750868")
                    && !e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())
                    && e.getMember() != null) {
                sendStaffChat("&e" + e.getMember().getNickname() + "&e(디코) : &f" + e.getMessage().getContentStripped());
            }
        }
    }

}
