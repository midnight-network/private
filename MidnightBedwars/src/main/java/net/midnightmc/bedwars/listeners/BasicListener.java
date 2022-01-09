package net.midnightmc.bedwars.listeners;

import com.destroystokyo.paper.MaterialTags;
import net.midnightmc.bedwars.BedwarsGame;
import net.midnightmc.bedwars.BedwarsManager;
import net.midnightmc.bedwars.BedwarsPlugin;
import net.midnightmc.bedwars.BedwarsTeam;
import net.midnightmc.bedwars.shop.BedwarsShopUtil;
import net.midnightmc.core.afk.AFKManager;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

public class BasicListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (RespawnManager.getInstance().isRespawning(player)) {
                e.setCancelled(true);
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                ScheduleUtil.sync(() -> BedwarsManager.getInstance().updateArmor(player), 1);
            }
        }
        if (e.getEntity() instanceof LivingEntity entity) {
            if (entity.getLocation().getY() < 0) {
                entity.setHealth(0);
            } else if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                e.setDamage(2);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> !BlockManager.isPlaceByPlayer(block));
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.POTION) {
            e.setReplacement(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotion(EntityPotionEffectEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (e.getModifiedType() == PotionEffectType.INVISIBILITY) {
            BedwarsManager.getInstance().updateArmor(player);
        }
    }

    @EventHandler
    public void onProjectHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Fireball fireball) {
            Location location = fireball.getLocation();
            location.getWorld().createExplosion(location, 5f, true, true, fireball);
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        if (e.getItemStack() != null) {
            e.getItemStack().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack itemMainHand = e.getPlayer().getInventory().getItemInMainHand();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && MaterialTags.BEDS.isTagged(e.getClickedBlock())) {
            if (itemMainHand.getType() == Material.AIR || !itemMainHand.getType().isBlock() || !e.getPlayer().isSneaking()) {
                e.setCancelled(true);
            }
        } else if (itemMainHand.getType() == Material.FIRE_CHARGE) {
            itemMainHand.setAmount(itemMainHand.getAmount() - 1);
            Player player = e.getPlayer();
            Location location = player.getEyeLocation();
            Fireball fireball = location.getWorld().spawn(location.add(location.getDirection().normalize().multiply(0.5)), Fireball.class);
            fireball.setDirection(location.getDirection().normalize());
            fireball.setShooter(player);
        } else if (BedwarsManager.getInstance().getGame(e.getPlayer().getUniqueId()) == null && itemMainHand.getType().equals(Material.COMPASS) &&
                (e.getAction().isRightClick() || e.getAction().isLeftClick())) {
            BedwarsGame game = BedwarsManager.getInstance().getGame(e.getPlayer().getWorld());
            if (game == null || game.getUUIDs().contains(e.getPlayer().getUniqueId())) {
                return;
            }
            game.openSpectatorGUI(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWaterMove(BlockFromToEvent e) {
        Material material = e.getToBlock().getType();
        if (MaterialTags.SKULLS.isTagged(material)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        PlayerInventory inventory = e.getPlayer().getInventory();
        if (item.getType() == Material.WOODEN_SWORD) {
            if ((!inventory.contains(Material.STONE_SWORD) && !inventory.contains(Material.IRON_SWORD) && !inventory.contains(Material.DIAMOND_SWORD)) ||
                    inventory.contains(Material.WOODEN_SWORD, 2)) {
                e.setCancelled(true);
            }
        } else if (item.getType() != Material.WOODEN_SWORD && MaterialTags.SWORDS.isTagged(item.getType())) {
            e.getPlayer().getInventory().addItem(new ItemBuilder(Material.WOODEN_SWORD).setUnbreakable().build());
        } else if (MaterialTags.PICKAXES.isTagged(item.getType()) || MaterialTags.AXES.isTagged(item.getType())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickUp(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }
        if (e.getItem().getItemStack().getType() != Material.WOODEN_SWORD && MaterialTags.SWORDS.isTagged(e.getItem().getItemStack().getType())) {
            player.getInventory().remove(Material.WOODEN_SWORD);
        } else if (AFKManager.getInstace().getTime(player) > 30) {
            e.setCancelled(true);
        } else if (e.getItem().getItemStack().getType() == Material.IRON_INGOT &&
                BedwarsShopUtil.getMaterialAmount(player, Material.IRON_INGOT) + e.getItem().getItemStack().getAmount() > 64 * 3) {
            e.setCancelled(true);
            player.sendMessage(MessageUtil.parse(player, "bedwars-max-items-iron"));
        } else if (e.getItem().getItemStack().getType() == Material.GOLD_INGOT &&
                BedwarsShopUtil.getMaterialAmount(player, Material.GOLD_INGOT) + e.getItem().getItemStack().getAmount() > 64) {
            e.setCancelled(true);
            player.sendMessage(MessageUtil.parse(player, "bedwars-max-items-gold"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void genSplit(EntityPickupItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (!e.getItem().hasMetadata("gen")) {
            return;
        }
        BedwarsTeam team = BedwarsManager.getInstance().getBedwarsTeam(player.getUniqueId());
        if (team == null) {
            return;
        }
        e.getItem().getLocation().getNearbyPlayers(1, 1).stream().filter(p -> team.players.contains(p.getUniqueId())).forEach(p -> {
            if (p.equals(player)) {
                return;
            }
            p.getInventory().addItem(e.getItem().getItemStack());
        });
    }

    @EventHandler
    public void onEggSpawn(final PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) {
            return;
        }
        if (e.getClickedBlock() == null) {
            return;
        }
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
        Location location = e.getClickedBlock().getLocation().add(0, 1, 0);
        if (itemStack == null || !(itemStack.getItemMeta() instanceof SpawnEggMeta)) {
            return;
        }
        e.setUseItemInHand(Event.Result.DENY);

        EntityType type = EntityType.valueOf(itemStack.getType().name().substring(0, "_SPAWN_EGG".length()));
        Entity entity = location.getWorld().spawnEntity(location, type, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
        entity.setMetadata("spawner", new FixedMetadataValue(BedwarsPlugin.getPlugin(), player.getUniqueId()));
    }

}
