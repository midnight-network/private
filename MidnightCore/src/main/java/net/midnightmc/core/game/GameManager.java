package net.midnightmc.core.game;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import net.kyori.adventure.text.Component;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import net.midnightmc.core.utils.WorldUtil;
import net.midnightmc.core.world.MapAPI;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class GameManager<G extends Game, I extends GameInfo> implements Listener {

    private final HashMap<Integer, G> games = new HashMap<>();
    private final int gamesCount;

    protected GameManager(int count, Plugin plugin) {
        this.gamesCount = count;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        schedule();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void schedule() {
        ScheduleUtil.async(() -> {
            while (true) {
                /*
                잘못된 게임 삭제
                 */
                Iterator<Map.Entry<Integer, G>> i = games.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<Integer, G> entry = i.next();
                    if (entry.getValue() == null || entry.getValue().getWorld() == null) {
                        i.remove();
                    } else if (entry.getValue().getStatus() == Game.GameStatus.ENDED) {
                        WorldUtil.deleteWorld(entry.getValue().getWorld());
                        Bukkit.getConsoleSender().sendMessage(Component.text(entry.getValue().getWorld().getName() + ": 언로드 완료"));
                        i.remove();
                    }
                }

                /*
                새로운 게임 로드
                 */
                if (games.size() < gamesCount) {
                    load();
                }

                /*
                2초 sleep
                 */
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void load() {
        int id = getAvailableID();
        if (id == -1) {
            return;
        }
        String worldname = "games" + File.separator + "game_" + id;
        FileUtils.deleteQuietly(new File(Bukkit.getWorldContainer(), worldname));
        World world;
        try {
            world = ScheduleUtil.callSync(() -> new WorldCreator(worldname).generator("MidnightCore:VOID").createWorld()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        if (world == null) {
            FileUtils.deleteQuietly(new File(Bukkit.getWorldContainer(), worldname));
            return;
        }

        I info = getGameInfo();
        if (info == null) {
            Bukkit.getConsoleSender().sendMessage(
                    MessageUtil.getComponent("===========================\n&c게임 없음\n==========================="));
            return;
        }
        if (!MapAPI.getInstance().loadMap(info.getMap(), world, false)) {
            Bukkit.getConsoleSender().sendMessage(
                    MessageUtil.getComponent("===========================\n&c월드 로드 실패\n==========================="));
            WorldUtil.deleteWorld(world);
            return;
        }
        ScheduleUtil.sync(() -> {
            G game = loadGame(world, info);
            if (game == null) {
                return;
            }
            addGame(id, game);
            game.ready();
        });
    }

    public @NotNull Collection<G> getGames() {
        return Collections.unmodifiableCollection(games.values());
    }

    protected final int getAvailableID() {
        for (int i = 1; i <= gamesCount; i++) {
            if (games.containsKey(i)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    private void addGame(int id, @Nullable G game) {
        if (game == null) {
            return;
        }
        games.put(id, game);
    }

    public @Nullable G getGame(Player player) {
        return getGame(player.getUniqueId());
    }

    public @Nullable G getGame(UUID uuid) {
        return games.values().stream().filter(game -> game.getUUIDs().contains(uuid)).findFirst().orElse(null);
    }

    public @Nullable G getGame(World world) {
        return games.values().stream().filter(game -> game.getWorld().equals(world)).findFirst().orElse(null);
    }

    public int getIDByGame(@Nullable G game) {
        if (game == null) {
            return -1;
        }
        return games.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), game))
                .map(Map.Entry::getKey).findFirst().orElse(-1);
    }

    public @Nullable G getBestGame() {
        return games.values().stream().filter(Game::accept).max(Comparator.comparingInt(value -> value.getPlayers().size())).orElse(null);
    }

    protected abstract @Nullable I getGameInfo();

    /**
     * 게임 로드
     * Async 호출됨
     *
     * @param world 게임 월드
     * @param info  게임 정보
     * @return 게임
     */
    protected abstract @Nullable G loadGame(World world, I info);

    @EventHandler
    public final void onAsyncLogin(AsyncPlayerPreLoginEvent e) {
        for (Game game : getGames()) {
            if (game.getUUIDs().contains(e.getUniqueId())) {
                return;
            }
        }
        if (getBestGame() == null) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageUtil.getComponent("&cno empty game\n비어있는 게임이 없습니다."));
        }
    }

    @EventHandler
    public final void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null);

        Game game = getGame(e.getPlayer());
        if (game != null) {
            game.join(e.getPlayer());
            return;
        }

        game = getBestGame();
        if (game == null) {
            e.getPlayer().kick(MessageUtil.getComponent("&cerror"));
            return;
        }
        if (!game.join(e.getPlayer())) {
            e.getPlayer().kick(MessageUtil.getComponent("&cerror"));
        }
    }

    @EventHandler
    public final void onQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        G game = getGame(e.getPlayer());
        if (game != null && game.getStatus() != Game.GameStatus.IN_GAME) {
            game.quit(e.getPlayer());
        } else {
            e.getPlayer().setHealth(0);
        }
    }

    /*
    여러가지 이벤트
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        World to = e.getPlayer().getWorld();
        Game game = getGame(to);
        if (game == null) {
            return;
        }
        if (!game.getUUIDs().contains(e.getPlayer().getUniqueId())) {
            game.setSpectator(e.getPlayer());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        Game game = getGame(player.getUniqueId());
        if (game == null) {
            e.setCancelled(true);
            return;
        }
        if (game.getStatus() != Game.GameStatus.IN_GAME) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) {
            return;
        }
        Game game = getGame(player.getUniqueId());
        if (game == null) {
            e.setCancelled(true);
            return;
        }
        if (game.getStatus() != Game.GameStatus.IN_GAME) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(EntityPlaceEvent e) {
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }
        Game game = getGame(e.getPlayer().getUniqueId());
        if (game == null) {
            e.setCancelled(true);
            return;
        }
        if (e.getBlock().getLocation().getBlockY() < game.HEIGHT_MIN) {
            e.setCancelled(true);
            player.sendMessage(MessageUtil.parse(player, "game-place-height-min"));
        } else if (e.getBlock().getLocation().getBlockY() > game.HEIGHT_MAX) {
            e.setCancelled(true);
            player.sendMessage(MessageUtil.parse(player, "game-place-height-max"));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (getGame(e.getPlayer()) == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (getGame(player) == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (getGame(e.getPlayer()) == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent e) {
        if (e.getCollidedWith() instanceof Player player && getGame(player.getUniqueId()) == null) {
            e.setCancelled(true);
        }
    }

}
