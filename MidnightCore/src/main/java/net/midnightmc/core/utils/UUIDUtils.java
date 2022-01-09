package net.midnightmc.core.utils;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import net.lingala.zip4j.ZipFile;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.api.MidnightAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public final class UUIDUtils {

    public static UUID getUUID(String name) {
        UUID uuid = getUUIDFromString(name);
        if (uuid != null) {
            return uuid;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
        if (offlinePlayer != null) {
            return offlinePlayer.getUniqueId();
        }
        return null;
    }

    public static UUID getUUIDAsync(String name) {
        UUID uuid = getUUID(name);
        if (uuid != null) {
            return getUUID(name);
        }
        PlayerData data = MidnightAPI.getInstance().getMorphia().find(PlayerData.class).filter(Filters.eq("lastname", name))
                .iterator(new FindOptions().projection().include("uuid")
                        .sort(Sort.descending("lastjoin")).limit(1)).tryNext();
        if (data != null) {
            return data.getUuid();
        }
        return null;
    }

    public static UUID getUUIDFromString(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return UUID.fromString(uuid.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5"));
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    public static void zip(final File file, final File folder) throws IOException {
        Files.deleteIfExists(file.toPath());
        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.addFolder(folder);
        }
    }

    public static void unzip(final File file, String path) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.extractAll(path);
        }
    }

}
