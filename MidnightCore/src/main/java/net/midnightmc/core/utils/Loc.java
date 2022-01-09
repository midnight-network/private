package net.midnightmc.core.utils;

import dev.morphia.annotations.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Entity(useDiscriminator = false)
public class Loc {

    private String world;
    private double x = 0, y = 0, z = 0;
    private float yaw = 0, pitch = 0;
    private transient Location loc;

    public Loc() {
    }

    public static Loc getLoc(Location location) {
        Loc loc = new Loc();
        loc.set(location);
        return loc;
    }

    public Location get() {
        if (loc == null) {
            if (this.world == null) {
                loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
            }
            loc = new Location(Bukkit.getWorld(this.world), x, y, z, yaw, pitch);
        }
        return loc.clone();
    }

    public Location get(World world) {
        this.world = world.getName();
        loc = null;
        return get();
    }

    public void set(Location location) {
        if (location == null) {
            Bukkit.broadcast(MessageUtil.getComponent("&c오류:1"));
            return;
        }
        if (location.getWorld() == null) {
            this.world = Bukkit.getWorlds().get(0).getName();
        } else {
            this.world = location.getWorld().getName();
        }
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        loc = null;
    }

}