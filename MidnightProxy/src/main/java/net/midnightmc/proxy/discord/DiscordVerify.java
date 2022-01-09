//package net.midnightmc.proxy.discord;
//
//import lombok.Getter;
//import net.dv8tion.jda.api.EmbedBuilder;
//import net.dv8tion.jda.api.entities.Member;
//import net.dv8tion.jda.api.entities.MessageEmbed;
//import net.dv8tion.jda.api.events.GenericEvent;
//import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
//import net.dv8tion.jda.api.hooks.EventListener;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.event.ClickEvent;
//import net.md_5.bungee.api.CommandSender;
//import net.md_5.bungee.api.ProxyServer;
//import net.md_5.bungee.api.connection.ProxiedPlayer;
//import net.md_5.bungee.api.event.PlayerDisconnectEvent;
//import net.md_5.bungee.api.event.PostLoginEvent;
//import net.md_5.bungee.api.plugin.Command;
//import net.md_5.bungee.api.plugin.Listener;
//import net.md_5.bungee.event.EventHandler;
//import net.midnightmc.proxy.Util;
//import org.jetbrains.annotations.NotNull;
//
//import java.awt.*;
//import java.util.HashMap;
//import java.util.Objects;
//
//public class DiscordVerify extends Command implements EventListener, Listener {
//
//    @Getter
//    private static final DiscordVerify instance = new DiscordVerify();
//    private static final HashMap<ProxiedPlayer, Member> pending = new HashMap<>();
//    private final Component REQUEST = Component.text("")
//            .append(Util.getComponent("""
//                    &b==========================================
//                    &d&lDiscord &a&lVerification
//
//                    %discord%
//
//                    """))
//            .append(Util.getComponent("&7계속하시려면 &6/verify &7를 입력하세요 &7Enter &6/verify &7to continue.").clickEvent(ClickEvent.runCommand("verify")))
//            .append(Util.getComponent("""
//
//                    &c만약 요청을 하신적이 없다면 무시해주세요!
//                    &cPlease ignore this if you did not request this!
//                    &b=========================================="""));
//
//    private DiscordVerify() {
//        super("verify", null, "인증");
//    }
//
//    @Override
//    public void execute(CommandSender sender, String[] args) {
//
//    }
//
//    @Override
//    public void onEvent(@NotNull GenericEvent event) {
//        if (event instanceof SlashCommandEvent e) {
//            if (e.getName().equalsIgnoreCase("verify")) {
//                e.deferReply(true).addEmbeds(
//                        new EmbedBuilder().setColor(Color.LIGHT_GRAY).setTitle("Verify / 인증")
//                                .addField(new MessageEmbed.Field("cheking...", "", true))
//                                .setFooter("Midnight Network").build()).queue();
//                try {
//                    String username = Objects.requireNonNull(e.getOption("username")).getAsString();
//                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
//                    if (player == null || !player.isConnected()) {
//                        e.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Verify / 인증")
//                                .addField(new MessageEmbed.Field("플레이어가 온라인이 아닙니다!\nThe player is not online!", "", true))
//                                .setFooter("Midnight Network").build()).queue();
//                    }
//
//                    Util.sendMessage(player, REQUEST.replaceText(builder -> builder
//                            .match("%discord%").replacement(Objects.requireNonNull(e.getMember()).getEffectiveName())));
//                } catch (NullPointerException ex) {
//                    e.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Verify / 인증")
//                            .addField(new MessageEmbed.Field("올바른 닉네임을 입력하세요.\nEnter a valid username.", "", true))
//                            .setFooter("Midnight Network").build()).queue();
//                    return;
//                }
//
//                e.getHook().editOriginalEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Verify / 인증")
//                        .addField(new MessageEmbed.Field("인증이 성공적으로 요청되었습니다! 서버에서 안내를 따라 진행하세요.\n" +
//                                "The verification has been successfully requested! Follow the instructions on the server.", "", true))
//                        .setFooter("Midnight Network").build()).queue();
//            }
//        }
//    }
//
//    @EventHandler
//    public void onJoin(PostLoginEvent e) {
//        pending.remove(e.getPlayer());
//    }
//
//    @EventHandler
//    public void onQuit(PlayerDisconnectEvent e) {
//        pending.remove(e.getPlayer());
//    }
//}