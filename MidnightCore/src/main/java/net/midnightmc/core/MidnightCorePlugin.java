package net.midnightmc.core;

import lombok.Getter;
import net.midnightmc.core.afk.AFKManager;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.commands.CoinCommand;
import net.midnightmc.core.commands.LanguageCommand;
import net.midnightmc.core.commands.URLPaste;
import net.midnightmc.core.commands.WorldCommand;
import net.midnightmc.core.gui.GUIListener;
import net.midnightmc.core.listeners.EventListener;
import net.midnightmc.core.playerdata.PlayerDataManager;
import net.midnightmc.core.world.EmptyBiomeProvider;
import net.midnightmc.core.world.MapManager;
import net.midnightmc.core.world.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MidnightCorePlugin extends JavaPlugin {

    @Getter
    private static MidnightCorePlugin plugin;

    @Override
    public void onEnable() {
        MidnightCorePlugin.plugin = this;
        new MidnightAPI();
        AFKManager.getInstace().init();
        Bukkit.getCommandMap().register("midnight", new LanguageCommand());
        Bukkit.getCommandMap().register("midnight", new CoinCommand());
        Bukkit.getCommandMap().register("midnight", new WorldCommand());
        Bukkit.getCommandMap().register("midnight", AFKManager.getInstace());
        Bukkit.getCommandMap().register("midnight", new URLPaste());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(MapManager.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "midnight:playerdatasync");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "midnight:playerdatasync", PlayerDataManager.getInstance());
    }

    @Override
    public void onDisable() {
        MidnightAPI.getInstance().getHikari().close();
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (id == null) {
            return null;
        }
        if (id.equalsIgnoreCase("void")) {
            return VoidGenerator.getInstance();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        if (id == null) {
            return null;
        }
        if (id.equalsIgnoreCase("void") || id.equalsIgnoreCase("empty") || id.equalsIgnoreCase("plains")) {
            return EmptyBiomeProvider.getInstance();
        } else {
            return null;
        }
    }

}
