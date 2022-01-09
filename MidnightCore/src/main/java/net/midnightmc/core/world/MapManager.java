package net.midnightmc.core.world;

import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import net.midnightmc.core.utils.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;

/**
 * 맵 관리 시스템
 */
public class MapManager implements Listener {

    private static MapManager instance;
    private final HashMap<Integer, String> maps = new HashMap<>();

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    private String createWorldName(final int id) {
        return "maps" + File.separator + "map_" + id;
    }

    public World create(final String name) {
        int id = createID(name);
        if (id == -1) {
            return null;
        }
        Bukkit.getConsoleSender().sendMessage(MessageUtil.getComponent("[Map] " + name + " ID:" + id));
        String worldname = createWorldName(id);
        if (Bukkit.getWorld(worldname) != null) {
            return null;
        }
        World world = new WorldCreator(worldname).environment(World.Environment.NORMAL).generator("MidnightCore:VOID").createWorld();
        if (world == null) {
            return null;
        }
        world.getWorldBorder().setSize(50);
        return world;
    }

    public void load(CommandSender sender, final String name) {
        int id = createID(name);
        if (id == -1) {
            World world = getWorldByMap(name);
            if (world != null) {
                sender.sendMessage(MessageUtil.getComponent("&c실패 &7이미 로드된 맵: " + world.getName()));
            } else {
                sender.sendMessage(MessageUtil.getComponent("&c실패 &7오류 발생"));
            }
            return;
        }
        String worldname = createWorldName(id);
        if (Bukkit.getWorld(worldname) != null) {
            return;
        }
        World world = new WorldCreator(worldname).generator("MidnightCore:VOID").createWorld();
        if (world == null) {
            return;
        }
        ScheduleUtil.async(() -> {
            if (MapAPI.getInstance().loadMap(name, world, true)) {
                sender.sendMessage(MessageUtil.getComponent("&a성공"));
            } else {
                sender.sendMessage(MessageUtil.getComponent("&c실패 &7콘솔 참고"));
                ScheduleUtil.async(() -> WorldUtil.deleteWorld(world));
            }
        });
    }

    public void save(CommandSender sender, final World world) {
        int id = getIDByWorld(world);
        if (id == -1) {
            sender.sendMessage(MessageUtil.getComponent("&c맵 번호 찾기 실패"));
            return;
        }
        ScheduleUtil.async(() -> {
            if (MapAPI.getInstance().saveMap(maps.get(id), world)) {
                sender.sendMessage(MessageUtil.getComponent("&a성공"));
            } else {
                sender.sendMessage(MessageUtil.getComponent("&c실패 &7콘솔 참고"));
            }
        });
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        maps.remove(getIDByWorld(e.getWorld()));
    }

    private int getIDByWorld(World world) {
        if (!world.getName().startsWith("maps" + File.separator + "map_")) {
            return -1;
        }
        String id = world.getName().substring(("maps" + File.separator + "map_").length());
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private @Nullable World getWorldByMap(@NotNull String map) {
        int id = maps.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(map)).findFirst().orElse(new AbstractMap.SimpleEntry<>(0, null)).getKey();
        return Bukkit.getWorld(createWorldName(id));
    }

    /**
     * create ID for the map
     *
     * @param name the name of the map
     * @return the new id or -1 if the map is already loaded
     */
    private int createID(@Nullable final String name) {
        if (name == null) {
            return -1;
        }
        if (maps.entrySet().stream().anyMatch(entry -> entry.getValue().equalsIgnoreCase(name))) {
            return -1;
        }
        int id = 0;
        while (true) {
            if (id > 20) {
                return -1;
            }
            if (Bukkit.getWorld(createWorldName(++id)) == null) {
                maps.put(id, name);
                return id;
            }
        }
    }

    public String getMapByWorld(World world) {
        return maps.get(getIDByWorld(world));
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        String map = getMapByWorld(e.getPlayer().getWorld());
        if (map != null) {
            e.getPlayer().setGameMode(GameMode.CREATIVE);
        }
    }

}
