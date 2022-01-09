package net.midnightmc.core.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;

public final class FaweUtil {

    public static boolean save(World world, File file) {
        return save(world, (int) (world.getWorldBorder().getSize() + 1) / 2, file);
    }

    public static boolean save(World world, int size, File file) {
        CuboidRegion region = new CuboidRegion(new BukkitWorld(world),
                BlockVector3.at(size, 0, size),
                BlockVector3.at(-world.getWorldBorder().getSize(), 255, -world.getWorldBorder().getSize()));
        FileUtils.deleteQuietly(file);
        try (Clipboard clipboard = Clipboard.create(region);
             ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
            writer.write(clipboard);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Paste the schematic to the world.
     *
     * @param world the world
     * @param file  the schematic file
     * @param loc   the location
     * @param air   whether to paste air
     * @return the size of the clipboard, or 0 if any error was occurred.
     */
    public static int paste(World world, File file, Location loc, boolean air) {
        int size;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             ClipboardReader reader = BuiltInClipboardFormat.FAST.getReader(bis);
             Clipboard clipboard = reader.read()) {
            BukkitWorld bukkitWorld = new BukkitWorld(world);
            size = Math.max(clipboard.getLength(), clipboard.getWidth());
            clipboard.paste(bukkitWorld, BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), false, air, false, null);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return size;
    }

    /**
     * Paste the schematic to the world.
     *
     * @param world the world
     * @param file  the schematic file
     * @param air   whether to paste air
     * @return the size of the clipboard, or 0 if any error was occurred.
     */
    public static int paste(World world, File file, boolean air) {
        return paste(world, file, new Location(world, 0, 0, 0), air);
    }

}
