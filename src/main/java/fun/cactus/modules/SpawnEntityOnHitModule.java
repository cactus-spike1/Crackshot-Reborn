package fun.cactus.modules;

import fun.cactus.utils.entuty.SpawnEntityData;
import org.bukkit.entity.EntityType;

public class SpawnEntityOnHitModule {
    boolean enable;
    int chance;
    String mobName;
    SpawnEntityData entityTypeBabyExplodeAmount;
    boolean makeEntitiesTargetVictim;
    int timedDeath;
    boolean entityDisableDrops;
    String messageShooter;
    String messageVictim;
}
