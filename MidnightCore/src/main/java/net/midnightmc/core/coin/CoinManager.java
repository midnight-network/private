package net.midnightmc.core.coin;

import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.api.EasyStatement;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.utils.UUIDUtils;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class CoinManager {

    private static final CoinManager instance;

    static {
        instance = new CoinManager();
    }

    public static CoinManager getInstance() {
        return instance;
    }

    public void check(UUID uuid) {
        try (Connection conn = MidnightAPI.getInstance().getHikari().getConnection()) {
            CachedRowSet rs = new EasyStatement(conn, "SELECT * FROM coins_update WHERE uuid=? AND is_done=0 ORDER BY time ASC")
                    .setArg(1, uuid.toString().replaceAll("-", ""))
                    .executeQuery();
            while (rs.next()) {
                if (apply(UUIDUtils.getUUIDFromString(
                                rs.getString("uuid")),
                        rs.getString("type").toUpperCase(),
                        rs.getInt("changes")
                )) {
                    new EasyStatement(conn, "UPDATE coins_update SET is_done=1 WHERE id=?")
                            .setArg(1, rs.getInt("id"))
                            .executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean apply(UUID uuid, String type, int amount) {
        ApplyType applyType;
        try {
            applyType = ApplyType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
        return apply(uuid, applyType, amount);
    }

    public boolean apply(UUID uuid, ApplyType type, int amout) {
        PlayerData data = PlayerData.get(uuid);
        if (data == null) {
            return false;
        }
        if (type == ApplyType.ADD) {
            data.setCoins(data.getCoins() + amout);
        } else if (type == ApplyType.SUBTRACT) {
            data.setCoins(data.getCoins() - amout);
        } else if (type == ApplyType.SET) {
            data.setCoins(amout);
        } else {
            return false;
        }
        data.save();
        return true;
    }

    public boolean query(UUID uuid, String type, int amout) {
        try (Connection conn = MidnightAPI.getInstance().getHikari().getConnection()) {
            CachedRowSet rs = new EasyStatement(conn, "SELECT id FROM coins_update WHERE is_done=1 ORDER BY id ASC LIMIT 1;").executeQuery();
            if (rs.first()) {
                new EasyStatement(conn, "UPDATE coins_update SET uuid=?,type=?,changes=?,time=?,is_done=0 WHERE id=?")
                        .setArg(1, uuid.toString().replaceAll("-", ""))
                        .setArg(2, type)
                        .setArg(3, amout)
                        .setArg(4, System.currentTimeMillis())
                        .setArg(5, rs.getInt("id"))
                        .executeUpdate();
            } else {
                new EasyStatement(conn, "INSERT INTO coins_update (uuid, type, changes, time) VALUES (?,?,?,?)")
                        .setArg(1, uuid.toString().replaceAll("-", ""))
                        .setArg(2, type)
                        .setArg(3, amout)
                        .setArg(4, System.currentTimeMillis())
                        .execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public enum ApplyType {
        ADD, SUBTRACT, SET
    }

}
