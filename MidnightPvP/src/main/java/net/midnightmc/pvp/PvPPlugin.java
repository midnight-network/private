package net.midnightmc.pvp;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPPlugin extends JavaPlugin {

    @Getter
    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        PvPPlugin.plugin = this;
        Bukkit.getPluginManager().registerEvents(PvPManager.getInstance(), this);
        Bukkit.getServer().getCommandMap().register("pvp", new PvPCommand());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
