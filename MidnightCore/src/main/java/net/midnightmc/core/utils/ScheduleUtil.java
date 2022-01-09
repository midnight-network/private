package net.midnightmc.core.utils;

import net.midnightmc.core.MidnightCorePlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class ScheduleUtil {

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), runnable);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(MidnightCorePlugin.getPlugin(), runnable);
    }

    public static void sync(Runnable runnable, int delay) {
        Bukkit.getScheduler().runTaskLater(MidnightCorePlugin.getPlugin(), runnable, delay);
    }

    public static <T> Future<T> callSync(Callable<T> callable) {
        return Bukkit.getScheduler().callSyncMethod(MidnightCorePlugin.getPlugin(), callable);
    }

}
