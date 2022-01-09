package net.midnightmc.bedwars;

import dev.morphia.annotations.Entity;
import net.midnightmc.core.game.GameInfo;
import net.midnightmc.core.utils.Loc;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;

@Entity(value = "GameInfo", useDiscriminator = false)
public class BedwarsInfo extends GameInfo {

    Loc spawn;
    int min, max, height_min = 0, height_max = 200;
    HashMap<Material, Generator> gens = new HashMap<>();
    ArrayList<Team> teams = new ArrayList<>();

    @Entity(useDiscriminator = false)
    public static class Generator {

        int max;
        int[] cooldowns;
        ArrayList<Loc> locations = new ArrayList<>();

        String name;
        Material head;

    }

    @Entity(useDiscriminator = false)
    public static class Team {

        String color;
        Loc spawn;
        Loc itemshop = new Loc();
        Loc upgradeshop = new Loc();

    }

}
