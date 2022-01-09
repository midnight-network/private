package net.midnightmc.bedwars;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BedwarsTeam {

    public final Set<UUID> players = new HashSet<>();
    @Getter
    private final NamedTextColor textColor;
    @Getter
    private final DyeColor dyeColor;
    @Getter
    private final Team sbteam;
    @Getter
    private final Location spawn;
    @Getter
    private final NPC itemshop, upgradeshop;
    public boolean hasBed = true;

    public int sharpness = 0, protection = 0, haste = 0;

    public BedwarsTeam(@NotNull NamedTextColor textColor, Team sbteam, Location spawn, Location itemshoploc, Location upgradeshoploc) {
        this.textColor = textColor;
        this.dyeColor = getDyeColorByTextColor(textColor);
        this.sbteam = sbteam;
        this.spawn = spawn;
        itemshop = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, LegacyComponentSerializer.legacyAmpersand().serialize(
                Component.text(textColor.toString().toUpperCase()).color(textColor).decorate(TextDecoration.BOLD)
                        .append(Component.text(" ITEM SHOP").color(NamedTextColor.WHITE))));
        itemshop.spawn(itemshoploc);
        upgradeshop = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, LegacyComponentSerializer.legacyAmpersand().serialize(
                Component.text(textColor.toString().toUpperCase()).color(textColor).decorate(TextDecoration.BOLD)
                        .append(Component.text(" UPGRADE SHOP").color(NamedTextColor.YELLOW))));
        upgradeshop.spawn(upgradeshoploc);
    }

    public List<Player> getOnlinePlayers() {
        return players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).toList();
    }

    private DyeColor getDyeColorByTextColor(NamedTextColor color) {
        return switch (color.toString().toUpperCase()) {
            case "RED" -> DyeColor.RED;
            case "BLUE" -> DyeColor.BLUE;
            case "YELLOW" -> DyeColor.YELLOW;
            case "GREEN" -> DyeColor.GREEN;
            case "AQUA" -> DyeColor.CYAN;
            case "GRAY" -> DyeColor.GRAY;
            case "LIGHT_PURPLE" -> DyeColor.PINK;
            case "DARK_PURPLE" -> DyeColor.PURPLE;
            default -> DyeColor.WHITE;
        };
    }

    @Override
    public String toString() {
        return "BedwarsTeam{\n" +
                "players=" + StringUtils.join(getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new), ", ") + "\n" +
                "textColor=" + textColor + "\n" +
                "dyeColor=" + dyeColor + "\n" +
                "sbteam=" + sbteam.getName() + "\n" +
                "hasbed=" + hasBed + "}";
    }

}
