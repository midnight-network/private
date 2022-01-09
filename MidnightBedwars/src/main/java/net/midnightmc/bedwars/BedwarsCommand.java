package net.midnightmc.bedwars;

import dev.morphia.query.experimental.filters.Filters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.midnightmc.bedwars.shop.BedwarsItem;
import net.midnightmc.core.api.MidnightAPI;
import net.midnightmc.core.game.GameInfo;
import net.midnightmc.core.utils.Loc;
import net.midnightmc.core.utils.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BedwarsCommand extends Command {

    public BedwarsCommand() {
        super("bedwars");
        setPermission("midnight.bedwars");
        permissionMessage(MessageUtil.getComponent("&c/bedwars"));
    }

    public Component getGameInfo(BedwarsGame game) {
        return MessageUtil.getComponent(
                "&6ID:&f " + BedwarsManager.getInstance().getIDByGame(game) + "\n" +
                        "&6Map:&f " + game.getMap() + "\n" +
                        "&6World:&f " + game.getWorld().getName() + "\n" +
                        "&6Status:&f " + game.getStatus() + "\n" +
                        "&6Countdown:&f " + game.getCountdown() + "\n" +
                        "&6Players:&f " + StringUtils.join(game.getPlayers().stream().map(Player::getName).toArray(String[]::new)) + "\n" +
                        "&6Teams:&f\n" + StringUtils.join(game.getTeams().stream().map(BedwarsTeam::toString).toArray(String[]::new), "\n") + "\n"
        );
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission(Objects.requireNonNull(getPermission()))) {
            return false;
        }
        if (args.length < 1) {
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "allinfo" -> {
                TextComponent.Builder builder = Component.text();
                for (BedwarsGame game : BedwarsManager.getInstance().getGames()) {
                    builder.append(getGameInfo(game)).append(Component.newline()).append(Component.newline());
                }
                sender.sendMessage(builder.build());
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    return false;
                }
                BedwarsGame game = BedwarsManager.getInstance().getGame(player.getUniqueId());
                if (game == null) {
                    game = BedwarsManager.getInstance().getGame(player.getWorld());
                    if (game == null) {
                        player.sendMessage(MessageUtil.getComponent("&c게임에 있지 않음"));
                        return false;
                    }
                }
                player.sendMessage(getGameInfo(game));
            }
            case "add" -> {
                BedwarsInfo info = new BedwarsInfo();
                info.setGame("bedwars-solo");
                info.setMap("test");
                info.spawn = Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
                info.min = 2;
                info.max = 2;

                BedwarsInfo.Generator gen;

                gen = new BedwarsInfo.Generator();
                gen.max = 48;
                gen.cooldowns = new int[]{1};
                gen.locations.add(Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)));
                gen.locations.add(Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 1, 0)));
                info.gens.put(Material.IRON_INGOT, gen);

                gen = new BedwarsInfo.Generator();
                gen.max = 4;
                gen.cooldowns = new int[]{30, 20};
                gen.locations.add(Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)));
                gen.locations.add(Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 1, 0)));
                gen.name = "&b&lDiamond";
                gen.head = Material.DIAMOND_BLOCK;
                info.gens.put(Material.DIAMOND, gen);

                BedwarsInfo.Team team = new BedwarsInfo.Team();
                team.spawn = Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
                team.color = NamedTextColor.RED.toString();
                info.teams.add(team);
                team = new BedwarsInfo.Team();
                team.spawn = Loc.getLoc(new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
                team.color = NamedTextColor.BLUE.toString();
                info.teams.add(team);

                MidnightAPI.getInstance().getMorphia().save(info);

                if (MidnightAPI.getInstance().getMorphia().find(GameInfo.class)
                        .filter(Filters.and(Filters.eq("game", "bedwars-solo"), Filters.eq("name", "test")))
                        .first() instanceof BedwarsInfo test) {
                    if (test.equals(info)) {
                        sender.sendMessage(MessageUtil.getComponent("&a성공"));
                    }
                }
            }
            case "itemshop", "upgradeshop" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.getComponent("&c/bedwars itemshop <team>"));
                    return false;
                }
                if (!(sender instanceof Player player)) {
                    return false;
                }
                BedwarsGame game = BedwarsManager.getInstance().getGame(player.getWorld());
                if (game == null) {
                    player.sendMessage(MessageUtil.getComponent("&c게임에서 입력하세요."));
                    return false;
                }
                BedwarsInfo info = MidnightAPI.getInstance().getMorphia().find(BedwarsInfo.class)
                        .filter(Filters.eq("map", game.getMap())).first();
                if (info == null) {
                    player.sendMessage(MessageUtil.getComponent("&c오류!"));
                    return false;
                }
                BedwarsInfo.Team bwteam = info.teams.stream().filter(team -> team.color.equalsIgnoreCase(args[1])).findFirst().orElse(null);
                if (bwteam == null) {
                    player.sendMessage(MessageUtil.getComponent("&c팀 찾기 실패"));
                    return false;
                }
                if (args[0].equalsIgnoreCase("itemshop")) {
                    bwteam.itemshop = Loc.getLoc(player.getLocation());
                } else {
                    bwteam.upgradeshop = Loc.getLoc(player.getLocation());
                }
                MidnightAPI.getInstance().getMorphia().save(info);
                player.sendMessage(MessageUtil.getComponent("&a성공"));
            }
            case "item" -> {
                if (!(sender instanceof Player player)) {
                    return false;
                }
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getComponent("사용법: /bedwars item <name>"));
                    return false;
                }
                String name = args[1];
                /*
                목록 /bedwars item list
                */
                if (name.equalsIgnoreCase("list")) {
                    TextComponent.Builder builder = Component.text();
                    builder.append(Component.text("배드워즈 아이템 목록").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
                    MidnightAPI.getInstance().getMorphia().find(BedwarsItem.class)
                            .forEach(item -> {
                                builder.append(Component.text(item.name).decorate(TextDecoration.BOLD).decorate(TextDecoration.UNDERLINED)).append(Component.newline());
                                item.prices.forEach((s, price) ->
                                        builder.append(Component.text(s + ": " + price.material + "," + price.amount)).append(Component.newline()));
                                builder.append(Component.newline());
                            });
                    player.sendMessage(builder.build());
                    return true;
                }
                if (args.length > 2) {
                    switch (args[2]) {
                        /*
                        삭제 /bedwars item <name> delete
                        */
                        case "delete" -> {
                            MidnightAPI.getInstance().getMorphia()
                                    .find(BedwarsItem.class)
                                    .filter(Filters.eq("name", name))
                                    .delete();
                            player.sendMessage(MessageUtil.getComponent("&a성공"));
                            return true;
                        }
                        /*
                        가격 설정 /bedwars item <name> setprice <game> <material> <amount>
                        */
                        case "setprice" -> {
                            if (args.length < 6) {
                                player.sendMessage(MessageUtil.getComponent("사용법: /bedwars item <name> setprice <game> <material> <amount>"));
                                return false;
                            }
                            Material material = switch (args[4].toLowerCase()) {
                                case "iron" -> Material.IRON_INGOT;
                                case "gold" -> Material.GOLD_INGOT;
                                case "diamond" -> Material.DIAMOND;
                                case "emerald" -> Material.EMERALD;
                                default -> null;
                            };
                            if (material == null) {
                                player.sendMessage(MessageUtil.getComponent("&cmaterial 알 수 없음"));
                                return false;
                            }
                            int amount;
                            try {
                                amount = Integer.parseInt(args[5]);
                            } catch (NumberFormatException ex) {
                                player.sendMessage(MessageUtil.getComponent("&camount 숫자로"));
                                return false;
                            }
                            BedwarsItem item = MidnightAPI.getInstance().getMorphia().find(BedwarsItem.class).filter(Filters.eq("name", name)).first();
                            if (item == null) {
                                player.sendMessage(MessageUtil.getComponent("&c존재하지 않는 아이템", "먼저 아이템을 생성하세요",
                                        "손에 아이템 들고, /bedwars item <name>"));

                                return false;
                            }
                            item.prices.put(args[3], new BedwarsItem.Price(material, amount));
                            MidnightAPI.getInstance().getMorphia().save(item);
                            player.sendMessage(MessageUtil.getComponent("&a성공"));
                            return true;
                        }
                    }
                }
                BedwarsItem item = new BedwarsItem();
                item.name = name;
                item.setItem(player.getInventory().getItemInMainHand());
                MidnightAPI.getInstance().getMorphia().save(item);
                player.sendMessage(MessageUtil.getComponent("&a성공"));
                player.sendMessage(MessageUtil.getComponent(
                        "이름:" + name,
                        "아이템:" + item.getItem().toString()));
            }
            default -> sender.sendMessage(MessageUtil.getComponent("&c잘못된 사용법"));
        }
        return false;
    }

}
