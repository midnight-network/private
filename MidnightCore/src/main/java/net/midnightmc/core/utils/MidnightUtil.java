package net.midnightmc.core.utils;

import net.midnightmc.core.MidnightCorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class MidnightUtil {

    private MidnightUtil() {}

    public static boolean isThisMyIpAddress(String address) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            return false;
        }
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
            return true;
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    public static void sendPlayer(Player player, String server) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(output)) {
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(MidnightCorePlugin.getPlugin(), "BungeeCord", output.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
