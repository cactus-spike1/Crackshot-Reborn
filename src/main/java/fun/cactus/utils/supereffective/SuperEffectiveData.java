package fun.cactus.utils.supereffective;

import org.bukkit.entity.EntityType;

public class SuperEffectiveData {

    private final EntityType entityType;
    private final double multiplier;

    public SuperEffectiveData(
            EntityType entityType,
            double multiplier
    ) {
        this.entityType = entityType;
        this.multiplier = multiplier;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
