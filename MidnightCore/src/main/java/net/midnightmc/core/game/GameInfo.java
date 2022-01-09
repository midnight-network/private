package net.midnightmc.core.game;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Entity(value = "GameInfo", useDiscriminator = false)
public class GameInfo {

    @Id
    private ObjectId id;
    @Getter
    @Setter
    @Indexed
    private String game;
    @Getter
    @Setter
    private String map;

}
