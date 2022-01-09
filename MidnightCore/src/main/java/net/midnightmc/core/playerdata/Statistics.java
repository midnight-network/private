package net.midnightmc.core.playerdata;

import dev.morphia.annotations.Entity;

@Entity(value = "Statistics", useDiscriminator = false)
public class Statistics {

    Statistics() {}

    public int pvp_kills, bedwars_kills, bedwars_final_kills, bedwars_beds_broken;

}
