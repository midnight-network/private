package net.midnightmc.bedwars;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import dev.morphia.query.experimental.filters.Filters;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.midnightmc.bedwars.listeners.BasicListener;
import net.midnightmc.bedwars.listeners.BlockItemPut;
import net.midnightmc.bedwars.listeners.BlockManager;
import net.midnightmc.bedwars.listeners.RespawnManager;
import net.midnightmc.bedwars.shop.BedwarsItem;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.game.GameManager;
import net.midnightmc.core.utils.FaweUtil;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.S3Util;
import net.midnightmc.core.utils.ScheduleUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BedwarsManager extends GameManager<BedwarsGame, BedwarsInfo> implements Listener {

    @Getter
    private static final BedwarsManager instance = new BedwarsManager();
    @Getter
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private BedwarsManager() {
        super(2, BedwarsPlugin.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BasicListener(), BedwarsPlugin.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BlockItemPut(), BedwarsPlugin.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BlockManager(), BedwarsPlugin.getPlugin());
        Bukkit.getPluginManager().registerEvents(RespawnManager.getInstance(), BedwarsPlugin.getPlugin());
        FloatingItem.enable(BedwarsPlugin.getPlugin());
        HandlerList.unregisterAll(instance);
        Bukkit.clearRecipes();
        MidnightAPI.getInstance().mapClasses(BedwarsInfo.class, BedwarsItem.class);
        protocolManager.addPacketListener(new PacketAdapter(BedwarsPlugin.getPlugin(), ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    int entityid = packet.getIntegers().read(0);
                    Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getEntityId() == entityid).findFirst().orElse(null);
                    if (player == null) {
                        return;
                    }
                    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(Material.AIR)));
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.AIR)));
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.AIR)));
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
                        packet.getSlotStackPairLists().write(0, pairList);
                    }
                }
            }
        });
    }

    public void updateArmor(Player player) {
        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, player.getEntityId());
        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, player.getEquipment().getHelmet()));
        pairList.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, player.getEquipment().getChestplate()));
        pairList.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, player.getEquipment().getLeggings()));
        pairList.add(new Pair<>(EnumWrappers.ItemSlot.FEET, player.getEquipment().getBoots()));
        packet.getSlotStackPairLists().write(0, pairList);
        player.getWorld().getPlayers().forEach(p -> {
            try {
                BedwarsManager.getInstance().getProtocolManager().sendServerPacket(p, packet);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
        });
    }

    public @Nullable BedwarsTeam getBedwarsTeam(UUID uuid) {
        BedwarsGame game = getGame(uuid);
        if (game == null) {
            return null;
        }
        return game.getTeam(uuid);
    }

    @Override
    protected BedwarsInfo getGameInfo() {
        return MidnightAPI.getInstance().getMorphia().find(BedwarsInfo.class).filter(Filters.eq("game", "bedwars-4")).first();
    }

    @Override
    protected @Nullable BedwarsGame loadGame(World world, BedwarsInfo info) {
        ScheduleUtil.async(() -> {
            File cache_waitingroom = new File(world.getWorldFolder(), "waitingroom.cache");
            S3Util.download(cache_waitingroom, "maps/bedwars/waitingroom/wood");
            FaweUtil.paste(world, cache_waitingroom, info.spawn.get(world).subtract(0, 1, 0), false);
            FileUtils.deleteQuietly(cache_waitingroom);
        });
        return new BedwarsGame(world, info);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        MidnightAPI.getInstance().setBoardTitle(e.getPlayer(), MessageUtil.getComponent("&c&lBEDWARS"));
    }

    @EventHandler
    public void onClickNPC(PlayerInteractEntityEvent e) {
        BedwarsGame game = getGame(e.getPlayer().getUniqueId());
        if (game == null) {
            return;
        }
        for (BedwarsTeam team : game.getTeams()) {
            if (e.getRightClicked().equals(team.getItemshop().getEntity())) {
                game.getShopManager().openItemShop(e.getPlayer());
                return;
            } else if (e.getRightClicked().equals(team.getUpgradeshop().getEntity())) {
                game.getShopManager().openUpgradeShop(e.getPlayer());
                return;
            }
        }
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent e) {
        e.getNPC().getEntity().setSilent(true);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        Iterator<NPC> iterator = CitizensAPI.getNPCRegistry().iterator();
        while (iterator.hasNext()) {
            NPC npc = iterator.next();
            if (!npc.isSpawned() || npc.getEntity().getWorld().equals(e.getWorld())) {
                npc.destroy();
                iterator.remove();
            }
        }
    }

}
