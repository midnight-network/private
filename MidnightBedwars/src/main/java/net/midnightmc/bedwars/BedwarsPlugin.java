package net.midnightmc.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsPlugin extends JavaPlugin {

    private static JavaPlugin plugin;

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        BedwarsPlugin.plugin = this;
        //noinspection ResultOfMethodCallIgnored
        BedwarsManager.getInstance();
        Bukkit.getCommandMap().register("midnight", new BedwarsCommand());
    }

}
