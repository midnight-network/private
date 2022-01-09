package net.midnightmc.core.api;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariDataSource;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.coin.CoinEconomy;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.MidnightUtil;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bson.UuidRepresentation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MidnightAPI {

    @Getter
    private static MidnightAPI instance;

    @Getter
    private final HikariDataSource hikari = new HikariDataSource();
    @Getter
    private final Datastore morphia;
    @Getter
    private final Gson gson = new Gson();
    private final Chat vaultChat;
    @Getter
    private final HashMap<String, String> configuration = new HashMap<>();
    private final HashMap<String, HashMap<String, String>> messages = new HashMap<>();
    @Getter
    private final HashMap<UUID, FastBoard> boards = new HashMap<>();
    @Getter
    private final MiniMessage miniMessage = MiniMessage.builder().build();
    @Getter
    private final String IP;

    public MidnightAPI() {
        instance = this;
        final String DOMAIN_IP = "10.34.96.3";
        IP = MidnightUtil.isThisMyIpAddress(DOMAIN_IP) ? "localhost" : DOMAIN_IP;
        hikari.setMaximumPoolSize(10);
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        hikari.setJdbcUrl("jdbc:mariadb://" + getIP() + ":3306/mcnetwork");
        hikari.addDataSourceProperty("user", "server");
        hikari.addDataSourceProperty("password", "hswhsw610!");
        loadLang();
        loadConfig();
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            Bukkit.getPluginManager().disablePlugin(MidnightCorePlugin.getPlugin());
            throw new IllegalStateException("error with vault");
        }
        vaultChat = rsp.getProvider();
        Bukkit.getServicesManager().register(Economy.class, new CoinEconomy(), MidnightCorePlugin.getPlugin(), ServicePriority.Normal);

        morphia = Morphia.createDatastore(
                MongoClients.create(MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb://hswhsw:miner102030%21@" + getIP() + "/mcnetwork"))
                        .uuidRepresentation(UuidRepresentation.STANDARD).build()), "mcnetwork");
        morphia.ensureIndexes();
        mapClasses(PlayerData.class);
    }

    private void createBoard(Player player) {
        if (!player.isOnline()) {
            return;
        }
        boards.put(player.getUniqueId(), new FastBoard(player) {
            @Override
            protected boolean hasLinesMaxLength() {
                return false;
            }
        });
    }

    public void setBoardTitle(Player player, Component title) {
        if (!boards.containsKey(player.getUniqueId())) {
            createBoard(player);
        }
        boards.get(player.getUniqueId()).updateTitle(LegacyComponentSerializer.legacySection().serialize(title));
    }

    public void setBoardLines(Player player, Component[] lines) {
        if (!boards.containsKey(player.getUniqueId())) {
            createBoard(player);
        }
        List<Component> l = new ArrayList<>(List.of(lines));
        l.add(Component.empty());
        l.add(Component.text("MidnightMC.net").color(NamedTextColor.YELLOW));
        boards.get(player.getUniqueId()).updateLines(l.stream().map(line -> LegacyComponentSerializer.legacySection().serialize(line)).toList().toArray(String[]::new));
    }

    public void mapClasses(Class<?>... classes) {
        morphia.getMapper().map(classes);
        morphia.ensureIndexes();
    }

    public void loadConfig() {
        configuration.clear();
        try (Connection conn = getHikari().getConnection()) {
            CachedRowSet rs = new EasyStatement(conn, "SELECT * FROM configuration;").executeQuery();
            while (rs.next()) {
                configuration.put(rs.getString("name"), rs.getString("value"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setLang(UUID uuid, String lang) {
        PlayerData playerData = PlayerData.get(uuid);
        if (playerData.getLanguage() != null && playerData.getLanguage().equals(lang)) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), () -> {
            playerData.setLanguage(lang);
            playerData.save();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(MessageUtil.parse(player, "language-changed", null));
            }
        });
    }

    public String getLang(UUID uuid) {
        PlayerData data = PlayerData.get(uuid);
        if (data == null) {
            return "";
        }
        if (data.getLanguage() == null) {
            data.setLanguage("");
        }
        return data.getLanguage();
    }

    public void loadLang() {
        try (Connection conn = getHikari().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT lang, name, value FROM `language`;")) {
            while (rs.next()) {
                messages.putIfAbsent(rs.getString("lang"), new HashMap<>());
                messages.get(rs.getString("lang")).put(rs.getString("name"), rs.getString("value"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getPrefix(Player player) {
        return vaultChat.getPlayerPrefix(player);
    }

    public String getMessage(@Nullable String lang, @Nullable String name) {
        if (name == null) {
            return "";
        }
        if (lang == null || lang.isEmpty()) {
            lang = "EN";
        }
        return messages.getOrDefault(lang, messages.get("EN")).getOrDefault(name, "").replace("\r\n", "\n");
    }


}
