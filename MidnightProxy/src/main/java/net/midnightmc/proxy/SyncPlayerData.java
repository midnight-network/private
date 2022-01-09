package net.midnightmc.proxy;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SyncPlayerData {

    private static final ArrayList<UUID> pending = new ArrayList<>();

    @Subscribe
    public void onServerChange(ServerPreConnectEvent e, Continuation continuation) {
        if (e.getPlayer().getCurrentServer().isEmpty()) {
            continuation.resume();
            return;
        }
        MidnightProxy.getLogger().info(e.getPlayer().getCurrentServer().get().getServerInfo().getName());/*
        pending.add(e.getPlayer().getUniqueId());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(output)) {
            out.writeUTF("save");
            e.getPlayer().getCurrentServer().get().sendPluginMessage(() -> "midnight:playerdatasync", output.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            continuation.resumeWithException(new IOException());
            return;
        }
        try {
            await().atMost(10, TimeUnit.SECONDS).until(() -> !pending.contains(e.getPlayer().getUniqueId()));
        } catch (ConditionTimeoutException ex) {
            continuation.resumeWithException(new TimeoutException());
            return;
        }
        continuation.resume();*/
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        continuation.resume();
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        MidnightProxy.getLogger().info(e.getIdentifier().getId());
        if (!e.getIdentifier().getId().equals("midnight:playerdatasync")) {
            return;
        }
        if (!(e.getSource() instanceof Player player)) {
            return;
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(e.getData()))) {
            String subchannel = input.readUTF();
            MidnightProxy.getLogger().info(subchannel);
            if (subchannel.equals("saved")) {
                MidnightProxy.getLogger().info(player.getUsername() + ": saved");
                pending.remove(player.getUniqueId());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
