package net.midnightmc.bedwars;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemGenerator {

    private final static Vector ZERO_VECTOR = new Vector();

    private final HashMap<Integer, Integer> cooldowns = new HashMap<>();
    @Getter
    private final Material material;
    private final ArrayList<Location> locations = new ArrayList<>();

    /*
    animation
     */
    @Getter
    private final boolean animation;
    private final String name;
    private final ItemStack head;
    private ArrayList<FloatingItem> floatingItems = null;

    @Getter
    @Setter
    private int tier = 1;
    private int second = 0;
    private int max = 0;

    public ItemGenerator(Material material, boolean animation, String name, Material head) {
        this.material = material;
        this.animation = animation;
        this.name = name;
        this.head = head == null ? null : new ItemStack(head, 1);
    }

    public ItemGenerator(Material material) {
        this(material, false, null, null);
    }

    public ItemGenerator setLocations(List<Location> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
        return this;
    }

    public ItemGenerator setCooldowns(int... cooldowns) {
        this.cooldowns.clear();
        for (int i = 0; i < cooldowns.length; i++) {
            this.cooldowns.put(i + 1, cooldowns[i]);
        }
        return this;
    }

    public ItemGenerator setMax(int max) {
        this.max = max;
        return this;
    }

    public void init() {
        if (animation) {
            floatingItems = new ArrayList<>();
            for (Location location : locations) {
                FloatingItem f = new FloatingItem(location.add(0, 1, 0));
                f.spawn(head, true);
                floatingItems.add(f);
            }
        }
    }

    public void everySecond() {
        second--;
        if (second <= 0) {
            second = cooldowns.get(tier);
            for (Location location : locations) {
                long count = location.getNearbyEntitiesByType(Item.class, 1, 3, item -> item.getItemStack().getType() == material)
                        .stream().mapToInt(item -> item.getItemStack().getAmount()).sum();
                if (count >= max) {
                    continue;
                }
                Item item = location.getWorld().dropItem(location, new ItemStack(material));
                item.setThrower(null);
                item.setOwner(null);
                item.setVelocity(ZERO_VECTOR);
                item.setMetadata("gen", new FixedMetadataValue(BedwarsPlugin.getPlugin(), true));
            }
        }
        if (animation) {
            floatingItems.forEach(floatingItem -> floatingItem.setTexts("&eTier " + tier, name, "&e" + second + " seconds"));
        }
    }

}
