package net.midnightmc.bedwars;

import net.kyori.adventure.text.Component;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FloatingItem {

    private static final List<FloatingItem> items = new ArrayList<>();
    private static BukkitRunnable runnable;
    private final Location location;
    private final List<ArmorStand> texts = new ArrayList<>();
    private Location sameLocation;
    private ArmorStand armorStand;
    private boolean floatLoop;

    /**
     * Constructs a new floating item and adds it to the items list
     *
     * @param location The location to spawn item at
     */
    public FloatingItem(Location location) {
        this.location = location;
        this.floatLoop = true;

        items.add(this);
    }

    /**
     * @apiNote This needs to be run on onEnable in order to update properly
     */
    public static void enable(JavaPlugin plugin) {
        if (runnable != null) {
            runnable.cancel();
        }
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<FloatingItem> iterator = FloatingItem.getFloatingItems().iterator();
                while (iterator.hasNext()) {
                    FloatingItem floatingItem = iterator.next();
                    if (floatingItem == null) {
                        iterator.remove();
                        continue;
                    }
                    floatingItem.update();
                }
            }
        };
        runnable.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * Deletes all floating items on the server
     */
    public static void deleteAll() {
        getFloatingItems().forEach(FloatingItem::delete);
        getFloatingItems().clear();
    }

    /**
     * Deletes all floating items on the server
     *
     * @param world the world
     */
    public static void deleteAll(World world) {
        Iterator<FloatingItem> iterator = FloatingItem.getFloatingItems().iterator();
        while (iterator.hasNext()) {
            FloatingItem floatingItem = iterator.next();
            if (floatingItem == null) {
                iterator.remove();
            } else if (floatingItem.getLocation().getWorld().equals(world)) {
                iterator.remove();
                floatingItem.delete();
            }
        }
    }

    /**
     * Gets all registered floating items
     *
     * @return All floating items
     */
    public static List<FloatingItem> getFloatingItems() {
        return items;
    }

    /**
     * Spawns the floating item with the given text and item type
     *
     * @param itemStack The itemstack
     * @param big       Whether the item should be big or not
     */
    public void spawn(ItemStack itemStack, boolean big) {
        if (armorStand != null) {
            armorStand.remove();
        }
        armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.getEquipment().setHelmet(itemStack);
        armorStand.setVisible(false);
        armorStand.setSmall(!big);
        this.sameLocation = armorStand.getLocation();
    }

    /**
     * Updates the floating item
     */
    private void update() {
        Location location = armorStand.getLocation();

        if (!this.floatLoop) {
            location.add(0, 0.01, 0);
            location.setYaw((location.getYaw() + 7.5F));

            armorStand.teleport(location);

            if (armorStand.getLocation().getY() > (0.25 + sameLocation.getY()))
                this.floatLoop = true;
        } else {
            location.subtract(0, 0.01, 0);
            location.setYaw((location.getYaw() - 7.5F));

            armorStand.teleport(location);

            if (armorStand.getLocation().getY() < (-0.25 + sameLocation.getY()))
                this.floatLoop = false;
        }
    }

    /**
     * Deletes all text that the floating item has
     */
    public void deleteAllText() {
        texts.forEach(Entity::remove);
        texts.clear();
    }

    /**
     * Deletes this floating item
     */
    public void delete() {
        getFloatingItems().remove(this);
        if (armorStand != null) {
            armorStand.remove();
            armorStand = null;
        }
        deleteAllText();
    }

    public void setTexts(String... texts) {
        while (this.texts.size() < texts.length) {
            addTextLine();
        }
        ArrayList<ArmorStand> reversed = new ArrayList<>(this.texts);
        Collections.reverse(reversed);
        for (int i = 0; i < texts.length; i++) {
            reversed.get(i).customName(MessageUtil.getComponent(texts[i]));
        }
    }

    private void addTextLine() {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0.25D * texts.size(), 0), EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.customName(Component.empty());
        armorStand.setCustomNameVisible(true);
        armorStand.setVisible(false);
        texts.add(armorStand);
    }

    /**
     * Gets the location of the floating item
     *
     * @return The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the armorstand of the floating item
     *
     * @return The armorstand
     */
    public ArmorStand getArmorStand() {
        return armorStand;
    }

}