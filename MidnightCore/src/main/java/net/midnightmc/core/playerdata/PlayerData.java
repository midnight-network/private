package net.midnightmc.core.playerdata;

import dev.morphia.annotations.*;
import dev.morphia.query.experimental.filters.Filters;
import lombok.Getter;
import lombok.Setter;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.api.MidnightAPI;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Entity(value = "PlayerData", useDiscriminator = false)
public class PlayerData {

    private static final HashMap<UUID, PlayerData> players = new HashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MidnightCorePlugin.getPlugin(), () ->
                players.values().forEach(PlayerData::save), 0, 20L * 10);
    }

    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    @Getter
    private UUID uuid;
    @Indexed
    @Setter
    @Getter
    private String lastname;
    @Getter
    @Setter
    private String language;
    @Getter
    @Setter
    private int coins = 0;
    @Getter
    @Setter
    private LocalDateTime lastjoin;
    @Getter
    private final Statistics statistics;

    private PlayerData() {
        statistics = null;
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.statistics = new Statistics();
    }

    public static PlayerData get(UUID uuid) {
        return players.get(uuid);
    }

    public static void remove(UUID uuid) {
        players.remove(uuid);
    }

    public void save() {
        MidnightAPI.getInstance().getMorphia().save(this);
    }

    public static boolean load(UUID uuid, String name) {
        PlayerData playerData = MidnightAPI.getInstance().getMorphia().find(PlayerData.class).filter(Filters.eq("uuid", uuid)).first();
        if (playerData == null) {
            playerData = new PlayerData(uuid);
            playerData.setLastname(name);
        }
        players.put(uuid, playerData);
        playerData.setLastjoin(LocalDateTime.now());
        MidnightAPI.getInstance().getMorphia().save(playerData);
        return true;
    }

}
