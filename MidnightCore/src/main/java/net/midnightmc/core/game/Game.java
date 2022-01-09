package net.midnightmc.core.game;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Game {

    public final int MAX_PLAYERS, MIN_PLAYERS, HEIGHT_MIN, HEIGHT_MAX;
    @Getter
    private final World world;
    @Getter
    private final String game;
    @Getter
    private final String map;
    @Getter
    private final String mapname;
    private final Location spawn;
    private final HashSet<UUID> players = new HashSet<>();
    private final BukkitRunnable runnable;
    @Getter
    private GameStatus status = GameStatus.LOADING;
    @Getter
    private int countdown = 10;

    /**
     * @param world   게임 월드
     * @param game    게임 이름
     * @param map     게임 맵
     * @param mapname 게임 맵 이름
     * @param spawn   게임 스폰 (관전자 스폰)
     * @param min     최소 인원
     * @param max     최대 인원
     */
    protected Game(World world, String game, String map, String mapname, Location spawn, int min, int max, int height_min, int height_max) {
        this.world = world;
        this.game = game;
        this.map = map;
        this.mapname = mapname;
        this.spawn = spawn;
        this.MAX_PLAYERS = max;
        this.MIN_PLAYERS = min;
        this.HEIGHT_MIN = height_min;
        this.HEIGHT_MAX = height_max;
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                // 현재 상태가 카운트다운 일떄
                if (status == GameStatus.COUNTDOWN) {
                    countdown--;
                    world.getPlayers().forEach(p -> p.setLevel(countdown));
                    if (countdown == 10 || countdown == 5 || countdown == 4 || countdown == 3 || countdown == 2 || countdown == 1) {
                        MessageUtil.message(world.getPlayers(), "game-countdown", Map.of("time", String.valueOf(countdown)));
                        world.getPlayers().forEach(player -> {
                            if (countdown == 10) {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                            } else {
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            }
                        });
                    } else if (countdown == 0) {
                        status = GameStatus.IN_GAME;
                        onStart();
                    }
                }
                // 현재 상태가 게임중인데 아무도 없을때
                else if (status == GameStatus.IN_GAME && getUUIDs().size() <= 0) {
                    end();
                }
                everySecond();
            }
        };
    }

    public final void ready() {
        countdown = 10;
        runnable.runTaskTimer(MidnightCorePlugin.getPlugin(), 20L, 20L);
        status = GameStatus.WAITING;
    }

    /**
     * 플레이어를 받는지 확인
     *
     * @return 플레이어를 받으면 true, 아니면 false
     */
    public boolean accept() {
        return (status == GameStatus.COUNTDOWN || status == GameStatus.WAITING) && (players.size() < MAX_PLAYERS);
    }

    /**
     * 플레이어를 참여 시킴
     *
     * @param player 이 게임에 참여시킬 플레이어
     * @return 참여 성공 시 true, 실패 시 false
     */
    public boolean join(Player player) {
        if (status == GameStatus.IN_GAME) {
            if (players.contains(player.getUniqueId())) {
                player.setHealth(0);
                updateHidden(player);
                return true;
            }
            player.sendMessage(MessageUtil.getComponent("&cerror"));
            return false;
        }
        if (!accept()) {
            return false;
        }
        player.setGameMode(GameMode.ADVENTURE);
        players.add(player.getUniqueId());
        player.teleport(getSpawn().add(0, 2, 0));
        updateHidden(player);
        MessageUtil.message(world.getPlayers(), "game-join", Map.of(
                "player", MessageUtil.getParsedString(player.name()),
                "size", String.valueOf(players.size()),
                "max", String.valueOf(MAX_PLAYERS)));
        if (players.size() >= MIN_PLAYERS && status != GameStatus.COUNTDOWN) {
            status = GameStatus.COUNTDOWN;
            countdown = 20;
            getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f));
            MessageUtil.message(getPlayers(), "game-countdown-ready");
        }
        return true;
    }

    public void quit(Player player) {
        if ((getStatus() == GameStatus.WAITING || getStatus() == GameStatus.COUNTDOWN) && players.contains(player.getUniqueId())) {
            MessageUtil.message(world.getPlayers(), "game-quit", Map.of(
                    "player", MessageUtil.getParsedString(player.name()),
                    "size", String.valueOf(players.size()),
                    "max", String.valueOf(MAX_PLAYERS)));
            players.remove(player.getUniqueId());
            if (players.size() < MIN_PLAYERS) {
                status = GameStatus.WAITING;
                countdown = 20;
                world.getPlayers().forEach(p -> p.setLevel(countdown));
                MessageUtil.message(getPlayers(), "game-waiting-more");
            }
            return;
        }
        players.remove(player.getUniqueId());
    }

    public void updateHidden(Player player) {
        //만약 플레이어면
        if (getUUIDs().contains(player.getUniqueId())) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.getWorld().equals(getWorld())) {
                    if (getUUIDs().contains(p.getUniqueId())) {
                        player.showPlayer(MidnightCorePlugin.getPlugin(), p);
                        p.showPlayer(MidnightCorePlugin.getPlugin(), player);
                    } else {
                        player.hidePlayer(MidnightCorePlugin.getPlugin(), p);
                        p.showPlayer(MidnightCorePlugin.getPlugin(), player);
                    }
                } else {
                    player.hidePlayer(MidnightCorePlugin.getPlugin(), p);
                    p.hidePlayer(MidnightCorePlugin.getPlugin(), player);
                }
            });
        } else { //만약 관전자면
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.getWorld().equals(getWorld())) {
                    if (getUUIDs().contains(p.getUniqueId())) {
                        player.showPlayer(MidnightCorePlugin.getPlugin(), p);
                        p.hidePlayer(MidnightCorePlugin.getPlugin(), player);
                    } else {
                        player.showPlayer(MidnightCorePlugin.getPlugin(), p);
                        p.showPlayer(MidnightCorePlugin.getPlugin(), player);
                    }
                } else {
                    player.hidePlayer(MidnightCorePlugin.getPlugin(), p);
                    p.hidePlayer(MidnightCorePlugin.getPlugin(), player);
                }
            });
        }
    }

    /**
     * 게임 끝내기
     */
    protected final void end() {
        Bukkit.getConsoleSender().sendMessage(MessageUtil.getComponent(world.getName() + ": 게임 종료"));
        runnable.cancel();
        status = GameStatus.ENDING;
        onEnd();
        ScheduleUtil.sync(() -> {
            status = GameStatus.ENDED;
            for (Player player : getWorldPlayers()) {
                if (player != null && player.isOnline()) {
                    player.kick(Component.text("game end"));
                }
            }
            players.clear();
        }, 20 * 30); // 30초
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    /**
     * 게임이 시작될 때
     */
    protected abstract void onStart();

    /**
     * 게임이 끝날 때
     */
    protected abstract void onEnd();

    /**
     * 매 초 실행될 메소드
     */
    protected abstract void everySecond();

    public Collection<UUID> getUUIDs() {
        return Collections.unmodifiableSet(players);
    }

    public Collection<Player> getPlayers() {
        return players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public Collection<Player> getWorldPlayers() {
        return getWorld().getPlayers();
    }

    /**
     * 플레이어를 이 게임의 관전자로 변경
     *
     * @param player 관전자로 설정할 플레이어
     */
    public void setSpectator(@NotNull Player player) {
        quit(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getActivePotionEffects().clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
        player.teleport(getSpawn());
        player.setBedSpawnLocation(getSpawn(), true);
        updateHidden(player);
    }

    public enum GameStatus {
        LOADING, WAITING, COUNTDOWN, IN_GAME, ENDING, ENDED;

        public String toString() {
            return super.toString().toLowerCase().replace("_", " ");
        }
    }

}
