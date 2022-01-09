package net.midnightmc.core.commands;

import net.kyori.adventure.text.Component;
import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.utils.CommonPermissions;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.ScheduleUtil;
import net.midnightmc.core.utils.WorldUtil;
import net.midnightmc.core.world.MapAPI;
import net.midnightmc.core.world.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorldCommand extends BukkitCommand {

    private final Component NOPERM_MSG;

    public WorldCommand() {
        super("world");
        setPermission("midnight.world");
        permissionMessage(MessageUtil.getComponent("&c권한이 없습니다. No permission"));
        NOPERM_MSG = permissionMessage();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission(Objects.requireNonNull(getPermission()))) {
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage(MessageUtil.getComponent("&c알 수 없는 명령어"));
            return false;
        }
        switch (args[0]) {
            case "list" -> {
                StringBuilder sb = new StringBuilder();
                for (World world : Bukkit.getWorlds()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(world.getName());
                }
                sender.sendMessage(MessageUtil.getComponent("&b월드 목록: " + sb + "\n&7맵 목록 로드중..."));
                Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), () ->
                        sender.sendMessage(MessageUtil.getComponent("&b맵 목록: " + String.join(", ", MapAPI.getInstance().listMaps()))));
            }
            case "tp" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.getComponent("&c월드 이름을 입력하세요."));
                    break;
                }
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage(MessageUtil.getComponent("&c해당 이름의 월드를 찾을 수 없습니다."));
                    break;
                }
                if (sender instanceof Player player) {
                    player.teleport(world.getSpawnLocation());
                }
            }
            case "map" -> {
                if (!sender.hasPermission(CommonPermissions.MAP)) {
                    sender.sendMessage(NOPERM_MSG);
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.getComponent("&c잘못된 사용법. &b/world map <create/load/save>"));
                    return false;
                }
                switch (args[1]) {
                    /*case "config" -> {
                        if (args.length < 4) {
                            sender.sendMessage(MessageUtil.getComponent("&c꺼져"));
                            break;
                        }
                        if (!(sender instanceof Player player)) {
                            return false;
                        }
                        String map = MapManager.getInstance().getMapByWorld(player.getWorld());
                        if (map == null) {
                            player.sendMessage(MessageUtil.getComponent("&c맵을 로드하세요."));
                            break;
                        }
                        try (Connection conn = MidnightAPI.getInstance().getHikari().getConnection();
                             CachedRowSet rs = new EasyStatement(conn, "SELECT config FROM maps WHERE name=?").setArg(1, map).executeQuery()) {
                            if (!rs.next()) {
                                player.sendMessage(MessageUtil.getComponent("&c오류."));
                                break;
                            }
                            JsonObject object = JsonParser.parseString(rs.getString("config")).getAsJsonObject();
                            JsonObject edit = object.getAsJsonObject();
                            String[] splited = args[2].split("\\.");
                            for (String key : Arrays.copyOf(splited, splited.length - 1)) {
                                if (edit.getAsJsonObject(key) == null) {
                                    edit.add(key, new JsonObject());
                                }
                                edit = edit.getAsJsonObject(key);
                            }
                            if (args[3].startsWith("add:")) {
                                String add = args[3].substring(4);
                                if (edit.getAsJsonArray(splited[splited.length - 1]) == null) {
                                    edit.add(splited[splited.length - 1], new JsonArray());
                                }
                                JsonArray jsonArray = edit.getAsJsonArray(splited[splited.length - 1]);
                                if (add.equalsIgnoreCase("loc")) {
                                    jsonArray.add(MidnightAPI.getInstance().getGson().toJsonTree(Loc.getLoc(player.getLocation()), Loc.class));
                                } else {
                                    try {
                                        jsonArray.add(Integer.parseInt(add));
                                        break;
                                    } catch (NumberFormatException ignored) {
                                    }
                                    try {
                                        jsonArray.add(Double.parseDouble(add));
                                        break;
                                    } catch (NumberFormatException ignored) {
                                    }
                                    jsonArray.add(add);
                                }
                            } else {
                                if (args[3].equalsIgnoreCase("loc")) {
                                    edit.add(splited[splited.length - 1], MidnightAPI.getInstance().getGson().toJsonTree(Loc.getLoc(player.getLocation()), Loc.class));
                                } else {
                                    try {
                                        edit.addProperty(splited[splited.length - 1], Boolean.parseBoolean(args[3]));
                                        break;
                                    } catch (NumberFormatException ignored) {
                                    }
                                    try {
                                        edit.addProperty(splited[splited.length - 1], Integer.parseInt(args[3]));
                                        break;
                                    } catch (NumberFormatException ignored) {
                                    }
                                    edit.addProperty(splited[splited.length - 1], args[3]);
                                }
                            }
                            new EasyStatement(conn, "UPDATE maps SET config=? WHERE name=?;")
                                    .setArg(1, MidnightAPI.getInstance().getGson().toJson(object)).setArg(2, map).execute();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }*/
                    case "create" -> {
                        if (!sender.hasPermission(CommonPermissions.MAP_CREATE)) {
                            sender.sendMessage(NOPERM_MSG);
                            return false;
                        }
                        if (args.length < 3) {
                            sender.sendMessage(MessageUtil.getComponent("&c맵 이름을 입력하세요."));
                            break;
                        }
                        World world = MapManager.getInstance().create(args[2]);
                        if (world != null) {
                            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                            sender.sendMessage(MessageUtil.getComponent("&a성공"));
                            if (sender instanceof Player player) {
                                player.teleport(world.getSpawnLocation());
                            }
                        } else {
                            sender.sendMessage(MessageUtil.getComponent("&c실패"));
                        }
                    }
                    case "load" -> {
                        if (args.length < 3) {
                            sender.sendMessage(MessageUtil.getComponent("&c맵 이름을 입력하세요."));
                            break;
                        }
                        MapManager.getInstance().load(sender, args[2]);
                    }
                    case "save" -> {
                        World world;
                        if (sender instanceof Player player) {
                            world = player.getWorld();
                        } else if (args.length >= 3) {
                            world = Bukkit.getWorld(args[2]);
                            if (world == null) {
                                sender.sendMessage(MessageUtil.getComponent("&c해당 이름의 월드를 찾을 수 없습니다."));
                                break;
                            }
                        } else {
                            sender.sendMessage(MessageUtil.getComponent("&c월드 이름을 입력하세요."));
                            break;
                        }
                        MapManager.getInstance().save(sender, world);
                    }
                }
            }
            case "load" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.getComponent("&c맵 이름을 입력하세요."));
                    break;
                }
                if (Bukkit.getWorld(args[1]) != null) {
                    sender.sendMessage(MessageUtil.getComponent("&c이미 해당 이름의 월드가 존재합니다."));
                }
                World world = new WorldCreator(args[1]).generator("MidnightCore:VOID").createWorld();
                if (world != null) {
                    sender.sendMessage(MessageUtil.getComponent("&a생성됨"));
                } else {
                    sender.sendMessage(MessageUtil.getComponent("&c오류"));
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.getComponent("&c맵 이름을 입력하세요."));
                    break;
                }
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage(MessageUtil.getComponent("&c해당 이름의 월드를 찾을 수 없습니다."));
                    break;
                }
                sender.sendMessage(MessageUtil.getComponent("&7삭제중.."));
                ScheduleUtil.async(() -> {
                    if (WorldUtil.deleteWorld(world)) {
                        sender.sendMessage(MessageUtil.getComponent("&a삭제 성공"));
                    } else {
                        sender.sendMessage(MessageUtil.getComponent("&c삭제 실패 &7콘솔 참조"));
                    }
                });
            }
            default -> sender.sendMessage(MessageUtil.getComponent("&c알 수 없는 명령어: " + args[0]));
        }
        return true;
    }

}
