package net.midnightmc.bedwars.shop;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Entity(value = "BedwarsItem", useDiscriminator = false)
public class BedwarsItem {

    @Indexed(options = @IndexOptions(unique = true))
    public String name;
    public byte[] item;
    @Property
    public HashMap<String, Price> prices = new HashMap<>();
    @Id
    private ObjectId id;

    public ItemStack getItem() {
        return ItemStack.deserializeBytes(item);
    }

    public void setItem(@Nullable final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        this.item = item.serializeAsBytes();
    }

    @Entity
    public static class Price {

        public Material material;
        public int amount;

        public Price(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }

        public Price() {}

    }

}
