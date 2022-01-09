package net.midnightmc.lobby;

import org.bukkit.plugin.java.JavaPlugin;

public class MidnightLobby extends JavaPlugin {

    @Override
    public void onEnable() {
        new LobbyCore(this);
    }

    @Override
    public void onDisable() {
        LobbyCore.getInstance().onDisable();
    }

}
