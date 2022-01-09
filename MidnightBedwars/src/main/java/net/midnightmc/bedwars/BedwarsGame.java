package net.midnightmc.bedwars;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.midnightmc.bedwars.listeners.RespawnManager;
import net.midnightmc.bedwars.shop.ShopArmorItem;
import net.midnightmc.bedwars.shop.ShopManager;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.coin.CoinManager;
import net.midnightmc.core.game.Game;
import net.midnightmc.core.gui.GUI;
import net.midnightmc.core.gui.GUIItem;
import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.ItemBuilder;
import net.midnightmc.core.utils.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class BedwarsGame extends Game {

    public final ArrayList<ItemGenerator> gens = new ArrayList<>();
    private final ArrayList<BedwarsTeam> teams = new ArrayList<>();
    private final HashMap<UUID, Integer> kills = new HashMap<>(), final_kills = new HashMap<>(), beds_broken = new HashMap<>();
    private final Scoreboard scoreboard;
    @Getter
    private final ShopManager shopManager;
    private int gameTime = 0;

    protected BedwarsGame(World world, BedwarsInfo info) {
        super(world, info.getGame(), info.getMap(), info.getMap().split("/")[1], info.spawn.get(world),
                info.min, info.max, info.height_min, info.height_max);
        shopManager = new ShopManager(this);
        info.gens.forEach((material, generator) -> {
            if (generator.name != null && generator.head != null) {
                gens.add(new ItemGenerator(material, true, generator.name, generator.head)
                        .setLocations(generator.locations.stream().map(loc -> loc.get(world)).toList())
                        .setCooldowns(generator.cooldowns)
                        .setMax(generator.max));
            } else {
                gens.add(new ItemGenerator(material)
                        .setLocations(generator.locations.stream().map(loc -> loc.get(world)).toList())
                        .setCooldowns(generator.cooldowns)
                        .setMax(generator.max));
            }
        });

        /*
        스코어보드 및 팀 설정
         */
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        info.teams.forEach(team -> {
            NamedTextColor color = NamedTextColor.NAMES.value(team.color.toLowerCase());
            if (color == null) {
                color = NamedTextColor.WHITE;
            }
            Team sbteam = scoreboard.registerNewTeam(color.toString());
            sbteam.setAllowFriendlyFire(false);
            sbteam.setCanSeeFriendlyInvisibles(true);
            sbteam.color(color);
            sbteam.prefix(Component.text("[" + color.toString().toUpperCase() + "] ").color(color).decorate(TextDecoration.BOLD));
            sbteam.setCanSeeFriendlyInvisibles(true);
            teams.add(new BedwarsTeam(
                    color,
                    sbteam,
                    team.spawn.get(world),
                    team.itemshop.get(world),
                    team.upgradeshop.get(world)
            ));
        });

        /*
        월드 설정
         */
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setFullTime(6000);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
    }

    public static void equipArmor(@Nullable BedwarsTeam team, Player player) {
        if (team == null) {
            return;
        }
        player.getInventory().setItem(EquipmentSlot.HEAD, new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable()
                .setLeatherArmorColor(team.getDyeColor())
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protection).build());
        player.getInventory().setItem(EquipmentSlot.CHEST, new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable()
                .setLeatherArmorColor(team.getDyeColor())
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protection).build());
        player.getInventory().setItem(EquipmentSlot.LEGS, new ItemBuilder(Material.getMaterial(ShopArmorItem.getArmorType(player) + "_LEGGINGS")).setUnbreakable()
                .setLeatherArmorColor(team.getDyeColor())
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protection).build());
        player.getInventory().setItem(EquipmentSlot.FEET, new ItemBuilder(Material.getMaterial(ShopArmorItem.getArmorType(player) + "_BOOTS")).setUnbreakable()
                .setLeatherArmorColor(team.getDyeColor())
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protection).build());
    }

    private void removeWaitingRoom() {
        BukkitWorld bukkitWorld = new BukkitWorld(getWorld());
        Location center = getSpawn().add(0, 5, 0);
        CuboidRegion region = new CuboidRegion((bukkitWorld),
                BlockVector3.at(center.getX() - 10, center.getY() - 10, center.getZ() - 10),
                BlockVector3.at(center.getX() + 10, center.getY() + 10, center.getZ() + 10));
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(bukkitWorld)) {
            BlockType blockType = BlockTypes.AIR;
            if (blockType == null) {
                return;
            }
            editSession.setBlocks((Region) region, blockType.getDefaultState());
        }
    }

    @Override
    public void onStart() {
        removeWaitingRoom();
        kills.clear();
        final_kills.clear();
        beds_broken.clear();

        gens.forEach(ItemGenerator::init);

        registerTeam();
        getPlayers().forEach(this::tpSpawn);
        MessageUtil.message(getPlayers(), "bedwars-start-message");
        getWorld().getPlayers().forEach(player -> {
            player.showTitle(
                    Title.title(MessageUtil.parse(player, "bedwars-start-title"),
                            MessageUtil.parse(player, "bedwars-start-subtitle"),
                            Title.Times.of(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        gameTime = 0;
    }

    private void registerTeam() {
        BedwarsTeam[] teams = this.teams.toArray(new BedwarsTeam[0]);
        ArrayList<UUID> players = new ArrayList<>(getUUIDs());
        Collections.shuffle(players);
        int i = 0;
        for (UUID uuid : players) {
            if (i >= teams.length) {
                i = 0;
            }
            teams[i++].players.add(uuid);
            kills.put(uuid, 0);
            final_kills.put(uuid, 0);
            beds_broken.put(uuid, 0);
        }
        getWorldPlayers().forEach(player -> {
            player.setScoreboard(scoreboard);
            setDisplayName(player);
        });
        getTeams().forEach(team -> {
            if (team.players.size() == 0) {
                team.hasBed = false;
            }
        });
    }

    private void setDisplayName(Player player) {
        BedwarsTeam team = getTeam(player.getUniqueId());
        if (team == null) {
            TextComponent displayname = Component.text()
                    .append(Component.text("[SPEC] ").color(NamedTextColor.GRAY))
                    .append(player.name().color(NamedTextColor.GRAY)).build();
            player.displayName(displayname);
            player.playerListName(displayname);
            return;
        }
        TextComponent.Builder builder = Component.text()
                .append(Component.text("[" + team.getTextColor().toString().toUpperCase() + "] ").color(team.getTextColor()));
        if (player.hasPermission(CommonPermissions.WHITECHAT)) {
            builder.append(player.name());
        } else {
            builder.append(player.name().color(NamedTextColor.GRAY));
        }
        Component displayname = builder.build();
        player.displayName(displayname);
        player.playerListName(displayname);
    }

    @Override
    public void onEnd() {
        RespawnManager.getInstance().onGameEnd(this);
        FloatingItem.deleteAll(getWorld());
        teams.forEach(team -> {
            team.getItemshop().destroy();
            team.getUpgradeshop().destroy();
        });
        BedwarsTeam win = teams.stream().filter(team -> team.players.size() > 0).findFirst().orElse(null);
        getWorld().getPlayers().forEach(player -> {
            if (getTeam(player.getUniqueId()) == win) {
                player.showTitle(Title.title(MessageUtil.parse(player, "bedwars-end-winner"), MessageUtil.getComponent(""),
                        Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ZERO)));
            } else {
                player.showTitle(Title.title(MessageUtil.parse(player, "bedwars-end"), MessageUtil.getComponent(""),
                        Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ZERO)));
            }
            MidnightAPI.getInstance().setBoardLines(player, MessageUtil.parseLines(player, "bedwars-scoreboard-gameover", Map.of(
                    "map", getMapname(),
                    "kills", String.valueOf(kills.getOrDefault(player.getUniqueId(), 0)),
                    "finalkills", String.valueOf(final_kills.getOrDefault(player.getUniqueId(), 0)),
                    "beds", String.valueOf(beds_broken.getOrDefault(player.getUniqueId(), 0))
            )));
        });
    }

    @Override
    public void everySecond() {
        getWorldPlayers().forEach(player -> player.setScoreboard(scoreboard));
        if (getStatus() == GameStatus.WAITING) {
            getWorldPlayers().forEach(player ->
                    MidnightAPI.getInstance().setBoardLines(player, MessageUtil.parseLines(player, "bedwars-scoreboard-waiting", Map.of(
                            "map", getMapname(),
                            "players", String.valueOf(getUUIDs().size()),
                            "max", String.valueOf(MAX_PLAYERS)
                    )))
            );
        } else if (getStatus() == GameStatus.COUNTDOWN) {
            getWorldPlayers().forEach(player ->
                    MidnightAPI.getInstance().setBoardLines(player, MessageUtil.parseLines(player, "bedwars-scoreboard-countdown", Map.of(
                            "time", String.valueOf(getCountdown()),
                            "map", getMapname(),
                            "players", String.valueOf(getUUIDs().size()),
                            "max", String.valueOf(MAX_PLAYERS)
                    )))
            );
        } else if (getStatus() == GameStatus.IN_GAME) {
            gameTime++;
            if (gameTime == 60 * 6) {
                gens.stream().filter(gen -> gen.getMaterial() == Material.DIAMOND).forEach(gen -> gen.setTier(2));
                MessageUtil.message(getWorld().getPlayers(), "bedwars-gen-upgrade", Map.of("gen", "Diamond", "tier", "2"));
            } else if (gameTime == 60 * 12) {
                gens.stream().filter(gen -> gen.getMaterial() == Material.EMERALD).forEach(gen -> gen.setTier(2));
                MessageUtil.message(getWorld().getPlayers(), "bedwars-gen-upgrade", Map.of("gen", "Emerald", "tier", "2"));
            } else if (gameTime == 60 * 18) {
                gens.stream().filter(gen -> gen.getMaterial() == Material.DIAMOND).forEach(gen -> gen.setTier(2));
                MessageUtil.message(getWorld().getPlayers(), "bedwars-gen-upgrade", Map.of("gen", "Diamond", "tier", "3"));
            } else if (gameTime == 60 * 24) {
                gens.stream().filter(gen -> gen.getMaterial() == Material.EMERALD).forEach(gen -> gen.setTier(2));
                MessageUtil.message(getWorld().getPlayers(), "bedwars-gen-upgrade", Map.of("gen", "Emerald", "tier", "3"));
            }
            if (gameTime == 60 * 30) {
                teams.forEach(team -> team.hasBed = false);
            } else if (gameTime == 60 * 35) {
                MessageUtil.message(getWorld().getPlayers(), "game-end-timer", Map.of("min", "5"));
            } else if (gameTime == 60 * 40) {
                end();
            }
            gens.forEach(ItemGenerator::everySecond);
            getWorldPlayers().forEach(player -> {
                ArrayList<Component> lines = new ArrayList<>();
                lines.add(Component.empty());
                //✓
                BedwarsTeam pteam = getTeam(player.getUniqueId());
                teams.forEach(team -> {
                    Component line = Component.text(team.getTextColor().toString().toUpperCase().charAt(0) + " ").color(team.getTextColor()).decorate(TextDecoration.BOLD)
                            .append(Component.text(StringUtils.capitalize(team.getTextColor() + " ")).color(NamedTextColor.WHITE));
                    if (team.hasBed) {
                        line = line.append(Component.text("✓").color(NamedTextColor.GREEN));
                    } else if (team.players.size() > 0) {
                        line = line.append(Component.text(team.players.size()).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
                    } else {
                        line = line.append(Component.text("✘").color(NamedTextColor.RED));
                    }
                    if (pteam != null && team.players.contains(player.getUniqueId())) {
                        line = line.append(Component.text(" <-YOU").color(NamedTextColor.GRAY));
                    }
                    lines.add(line);
                });
                lines.add(Component.empty());
                lines.add(Component.text("Kills: ").append(Component.text(kills.get(player.getUniqueId())).color(NamedTextColor.GREEN)));
                lines.add(Component.text("Final Kills: ").append(Component.text(final_kills.get(player.getUniqueId())).color(NamedTextColor.GREEN)));
                lines.add(Component.text("Beds Broken: ").append(Component.text(beds_broken.get(player.getUniqueId())).color(NamedTextColor.GREEN)));
                MidnightAPI.getInstance().setBoardLines(player, lines.toArray(Component[]::new));
            });
        }
    }

    @Override
    public boolean join(Player player) {
        if (!super.join(player)) {
            return false;
        }
        player.setScoreboard(scoreboard);
        if (getStatus() == GameStatus.IN_GAME) {
            Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getPlugin(), () -> player.spigot().respawn(), 1L);
            BedwarsTeam team = getTeam(player.getUniqueId());
            if (team == null) {
                return false;
            }
            team.getSbteam().addEntry(player.getName());
            setDisplayName(player);
        } else {
            ShopArmorItem.reset(player);
        }
        return true;
    }

    @Override
    public void quit(Player player) {
        super.quit(player);
        BedwarsTeam team = getTeam(player.getUniqueId());
        if (team == null) {
            ShopArmorItem.reset(player);
            return;
        }
        if (!team.hasBed) {
            ShopArmorItem.reset(player);
            team.players.remove(player.getUniqueId());
        }
        try {
            team.getSbteam().removeEntry(player.getName());
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public void setSpectator(@NotNull Player player) {
        super.setSpectator(player);
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).guiMode().build());
        setDisplayName(player);
    }

    @Override
    public void updateHidden(Player player) {
        if (!RespawnManager.getInstance().isRespawning(player)) {
            getPlayers().forEach(p -> p.showPlayer(BedwarsPlugin.getPlugin(), player));
        }
        super.updateHidden(player);
        if (RespawnManager.getInstance().isRespawning(player)) {
            getPlayers().forEach(p -> p.hidePlayer(BedwarsPlugin.getPlugin(), player));
        }
    }

    public void openSpectatorGUI(Player player) {
        List<Player> players = new ArrayList<>(getPlayers());
        Collections.shuffle(players);
        players = players.stream().limit(28).toList();
        GUI gui = new GUI("&6Players", (players.size() - 1) / 7 + 3);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int slot = 10 + (i / 7) * 2 + i;
            gui.setItem(slot, new GUIItem(new ItemBuilder(Material.PLAYER_HEAD).setSkullOwner(p.getUniqueId()).guiMode().setName(p.name()).build()).setExecute(pl -> {
                if (!p.isOnline()) {
                    return;
                }
                pl.teleport(p.getLocation());
            }));
        }
        gui.open(player);
    }

    public @Nullable BedwarsTeam getTeam(UUID uuid) {
        return teams.stream().filter(team -> team.players.contains(uuid)).findFirst().orElse(null);
    }

    public @NotNull List<BedwarsTeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public void tpSpawn(Player player) {
        BedwarsTeam team = getTeam(player.getUniqueId());
        if (team == null) {
            return;
        }
        equip(team, player);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setFallDistance(0);
        player.teleport(team.getSpawn());
        player.setGameMode(GameMode.SURVIVAL);
    }

    public void equip(@Nullable BedwarsTeam team, Player player) {
        if (team == null) {
            return;
        }
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemBuilder(Material.WOODEN_SWORD).setUnbreakable()
                .addEnchant(Enchantment.DAMAGE_ALL, team.sharpness).build());
        player.getInventory().addItem(getShopManager().getAxeTool().getItem(player));
        player.getInventory().addItem(getShopManager().getPickaxeTool().getItem(player));
        player.getInventory().addItem(getShopManager().getShearsTool().getItem(player));
        equipArmor(team, player);
    }

    public void destroyBed(Player player, BlockState state) {
        if (!(state.getBlockData() instanceof Bed bed)) {
            return;
        }
        BedwarsTeam team = getClosestTeam(state.getLocation());
        if (team == null || getTeam(player.getUniqueId()) == team) {
            return;
        }
        if (team.hasBed) {
            BedwarsTeam bwteam = getTeam(player.getUniqueId());
            if (bwteam == null) {
                return;
            }
            MessageUtil.message(getWorld().getPlayers(), "bedwars-bed-destroyed",
                    Map.of("player", MessageUtil.getParsedString(player.name().color(bwteam.getTextColor())),
                            "team", MessageUtil.getParsedString(Component.text(team.getTextColor().toString().toUpperCase()).color(team.getTextColor()))));
            addBadsBroken(player.getUniqueId());
            getWorld().getPlayers().forEach(p -> {
                if (team.players.contains(p.getUniqueId())) {
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
                } else {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                }
            });
            team.players.forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) {
                    return;
                }
                p.showTitle(Title.title(MessageUtil.parse(p, "bedwars-destroyed-title"),
                        MessageUtil.parse(p, "bedwars-destroyed-subtitle"),
                        Title.Times.of(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))));
            });
            team.hasBed = false;
        }
        if (bed.getPart() == Bed.Part.HEAD) {
            Block foot = state.getBlock().getRelative(((Bed) state.getBlockData()).getFacing().getOppositeFace());
            state.getBlock().setType(Material.AIR);
            foot.setType(Material.AIR);
        } else {
            Block head = state.getBlock().getRelative(((Bed) state.getBlockData()).getFacing());
            head.setType(Material.AIR);
            state.getBlock().setType(Material.AIR);
        }
    }

    public BedwarsTeam getClosestTeam(Location location) {
        BedwarsTeam bwteam = null;
        for (BedwarsTeam team : teams) {
            if (bwteam == null) {
                bwteam = team;
                continue;
            }
            if (location.distanceSquared(team.getSpawn()) < location.distanceSquared(bwteam.getSpawn())) {
                bwteam = team;
            }
        }
        return bwteam;
    }

    public void addKills(UUID uuid) {
        kills.put(uuid, kills.getOrDefault(uuid, 0) + 1);
    }

    public void addFinalKills(UUID uuid) {
        final_kills.put(uuid, final_kills.getOrDefault(uuid, 0) + 1);
        CoinManager.getInstance().apply(uuid, CoinManager.ApplyType.ADD, 5);
    }

    private void addBadsBroken(UUID uuid) {
        if (!beds_broken.containsKey(uuid)) {
            return;
        }
        beds_broken.put(uuid, beds_broken.get(uuid) + 1);
        CoinManager.getInstance().apply(uuid, CoinManager.ApplyType.ADD, 5);
    }

    /**
     * 게임 종료 확인
     *
     * @return 게임 종료면 true, 아니면 false
     */
    @SuppressWarnings("UnusedReturnValue")
    public final boolean checkGameEnd() {
        int remaining = 0;
        for (BedwarsTeam team : teams) {
            if (team.hasBed) remaining++;
            else if (team.getOnlinePlayers().size() > 0) remaining++;
            if (remaining > 1) {return false;}
        }
        end();
        return true;
    }

}
