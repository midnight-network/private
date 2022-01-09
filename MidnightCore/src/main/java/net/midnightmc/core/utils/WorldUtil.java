package net.midnightmc.core.utils;

import net.midnightmc.core.MidnightCorePlugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class WorldUtil {

    /**
     * 월드 삭제.
     * Async 추천
     *
     * @param world 삭제 할 월드
     * @return 성공했을 시 true
     */
    public static boolean deleteWorld(@Nullable final World world) {
        if (world == null) {
            return true;
        }
        File folder = world.getWorldFolder();
        try {
            if (Bukkit.getScheduler().callSyncMethod(MidnightCorePlugin.getPlugin(), () -> {
                for (Player player : world.getPlayers()) {
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                return Bukkit.unloadWorld(world, false);
            }).get()) {
                try {
                    FileUtils.deleteDirectory(folder);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get nearby blocks
     *
     * @param location the location
     * @param radius   the radius
     * @return the blocks in radius.
     */
    public static List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}
