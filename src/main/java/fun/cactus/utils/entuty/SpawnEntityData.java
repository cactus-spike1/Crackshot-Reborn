package fun.cactus.utils.entuty;

public class SpawnEntityData {

    private final String entityType;
    private final boolean baby;
    private final boolean explode;
    private final int amount;

    public SpawnEntityData(
            String entityType,
            boolean baby,
            boolean explode,
            int amount
    ) {
        this.entityType = entityType;
        this.baby = baby;
        this.explode = explode;
        this.amount = amount;
    }

    public String getEntityType() {
        return entityType;
    }

    public boolean isBaby() {
        return baby;
    }

    public boolean isExplode() {
        return explode;
    }

    public int getAmount() {
        return amount;
    }
}
