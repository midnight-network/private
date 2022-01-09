package net.midnightmc.bedwars.listeners;

import net.midnightmc.bedwars.BedwarsGame;
import net.midnightmc.bedwars.BedwarsManager;
import net.midnightmc.bedwars.BedwarsPlugin;
import net.midnightmc.core.game.Game;
import net.midnightmc.core.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class BlockManager implements Listener {

    /**
     * 블럭이 플레이어에 의해 설치되었는지 확인
     *
     * @param block 확인할 블럭
     * @return 플레이어가 설치하였으면 true, 아니면 false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPlaceByPlayer(Block block) {
        return block.hasMetadata("bw-placedbyplayer");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.TNT) {
            Location location = e.getBlock().getLocation();
            e.getBlock().setType(Material.AIR);
            location.getWorld().spawn(location.add(0.5, 0.5, 0.5), TNTPrimed.class);
            return;
        }
        e.getBlockPlaced().setMetadata("bw-placedbyplayer", new FixedMetadataValue(BedwarsPlugin.getPlugin(), true));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        BedwarsGame game = BedwarsManager.getInstance().getGame(e.getBlock().getWorld());
        if (game != null) {
            if (game.getStatus() != Game.GameStatus.IN_GAME) {
                e.setCancelled(true);
                return;
            }
            if (e.getBlock().getBlockData() instanceof Bed) {
                e.setCancelled(true);
                game.destroyBed(e.getPlayer(), e.getBlock().getState());
                return;
            }
        }

        if (!isPlaceByPlayer(e.getBlock())) {
            MessageUtil.message(List.of(e.getPlayer()), "bedwars-cannot-break", null);
            e.setCancelled(true);
        }
    }

}
