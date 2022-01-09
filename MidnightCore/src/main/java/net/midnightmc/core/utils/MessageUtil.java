package net.midnightmc.core.utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.midnightmc.core.BasicChatFormat;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.api.MidnightAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class MessageUtil {

    public static Component getComponent(String... string) {
        if (string.length == 0) {
            return Component.empty();
        } else if (string.length == 1) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(string[0]);
        } else {
            TextComponent component = Component.empty();
            for (String s : string) {
                component = component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(s)).append(Component.newline());
            }
            return component;
        }
    }

    public static void message(Collection<? extends Player> players, String name) {
        message(players, name, null);
    }

    public static void message(Collection<? extends Player> players, String name, @Nullable Map<String, String> replace) {
        HashMap<String, Component> cache = new HashMap<>();
        for (Player player : players) {
            String lang = MidnightAPI.getInstance().getLang(player.getUniqueId());
            if (!cache.containsKey(lang)) {
                Component component = parse(lang, name, replace);
                cache.put(lang, component);
            }
            player.sendMessage(cache.get(lang));
        }
    }

    public static String getParsedString(String string) {
        return getParsedString(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
    }

    public static String getParsedString(Component component) {
        return MidnightAPI.getInstance().getMiniMessage().serialize(component);
    }

    /**
     * parse minimessage
     *
     * @param string the string to be parsed
     * @return the parsed component
     */
    public static Component parse(String string) {
        return MidnightAPI.getInstance().getMiniMessage().deserialize(string);
    }

    /**
     * parse minimessage with placeholders
     *
     * @param string       the string to be parsed
     * @param placeholders the placeholders
     * @return the parsed component
     */
    public static Component parse(String string, @Nullable Map<String, String> placeholders) {
        if (placeholders == null) {
            return parse(string);
        }
        //noinspection UnstableApiUsage,deprecation
        return MidnightAPI.getInstance().getMiniMessage().parse(string, placeholders);
    }

    /**
     * parse minimessage with placeholders
     *
     * @param lang         the language
     * @param name         the name of the message
     * @param placeholders the placeholders
     * @return the parsed component
     */
    public static Component parse(String lang, String name, @Nullable Map<String, String> placeholders) {
        return parse(MidnightAPI.getInstance().getMessage(lang, name), placeholders);
    }

    /**
     * parse minimessage with placeholders
     *
     * @param player       the player
     * @param name         the name of the message
     * @param placeholders the placeholders
     * @return the parsed component
     */
    public static Component parse(Player player, String name, @Nullable Map<String, String> placeholders) {
        return parse(MidnightAPI.getInstance().getLang(player.getUniqueId()), name, placeholders);
    }

    /**
     * parse minimessage
     *
     * @param player the player
     * @param name   the name of the message
     * @return the parsed component
     */
    public static Component parse(Player player, String name) {
        return parse(player, name, null);
    }

    public static Component[] parseLines(String lang, String name, @Nullable Map<String, String> placeholders) {
        return MidnightAPI.getInstance().getMessage(lang, name).lines()
                .map(s -> parse(s, placeholders)).toArray(Component[]::new);
    }

    public static Component[] parseLines(Player player, String name, @Nullable Map<String, String> placeholders) {
        return parseLines(MidnightAPI.getInstance().getLang(player.getUniqueId()), name, placeholders);
    }

    public static void sendGlobalChat(Player player, Component prefix, AsyncChatEvent e) {
        sendGlobalChat(player, Component.text().append(prefix).append(Component.text(" "))
                .append(BasicChatFormat.getInstance().render(e.getPlayer(), e.getPlayer().displayName(), e.message(), Audience.audience(e.viewers())))
                .build());
    }

    public static void sendGlobalChat(Player player, Component component) {
        //player.sendMessage(Component.text("debug: ").append(component));
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("MessageRaw");
            out.writeUTF("ALL");
            out.writeUTF(PaperComponents.gsonSerializer().serialize(component));
            player.sendPluginMessage(MidnightCorePlugin.getPlugin(), "BungeeCord", b.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
