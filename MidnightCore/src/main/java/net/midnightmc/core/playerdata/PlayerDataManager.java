package net.midnightmc.core.playerdata;

import lombok.Getter;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.utils.ScheduleUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public final class PlayerDataManager implements PluginMessageListener {

    @Getter
    private static final PlayerDataManager instance = new PlayerDataManager();

    private PlayerDataManager() {}

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("midnight:playerdatasync")) {
            return;
        }
        Bukkit.getLogger().info(player.getName() + ":save");
        ScheduleUtil.async(() -> {
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
                String subchannel = input.readUTF();
                if (subchannel.equals("save")) {
                    PlayerData.get(player.getUniqueId()).save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(output)) {
                out.writeUTF("saved");
                player.sendPluginMessage(MidnightCorePlugin.getPlugin(), "midnight:playerdatasync", output.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
