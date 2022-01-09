package net.midnightmc.proxy;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class Util {

    public static Component getComponent(String str) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
    }

    public static void sendMessage(Audience sender, String msg) {
        sender.sendMessage(getComponent(msg));
    }

    public static Player getPlayer(String nameoruuid) {
        try {
            return MidnightProxy.getServer().getPlayer(nameoruuid).orElseGet(() ->
                    MidnightProxy.getServer().getPlayer(UUID.fromString(nameoruuid
                            .replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"))).orElse(null));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
