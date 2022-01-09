package net.midnightmc.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.midnightmc.proxy.command.DiscordCommand;
import net.midnightmc.proxy.command.KickCommand;
import net.midnightmc.proxy.command.SendCommand;
import net.midnightmc.proxy.command.StaffChatCommand;
import net.midnightmc.proxy.discord.DiscordManager;

import java.util.logging.Logger;

@Plugin(id = "midnightproxy", name = "MidnightProxy", version = "1.0")
public final class MidnightProxy {

    @Getter
    private static MidnightProxy plugin;
    @Getter
    private static ProxyServer server;
    @Getter
    private static Logger logger;

    @Inject
    public MidnightProxy(ProxyServer server, Logger logger) {
        MidnightProxy.plugin = this;
        MidnightProxy.server = server;
        MidnightProxy.logger = logger;
        DiscordManager.getInstance().init();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register("discord", new DiscordCommand(), "디코", "디스코드");
        server.getCommandManager().register("staffchat", new StaffChatCommand(), "sc");
        server.getCommandManager().register("kick", new KickCommand());
        server.getCommandManager().register("send", new SendCommand());
        server.getEventManager().register(this, DiscordManager.getInstance());
        server.getEventManager().register(this, new JoinLeaveMessage());
        server.getEventManager().register(this, StaffChat.getInstance());
        server.getEventManager().register(this, new SyncPlayerData());
    }

}
