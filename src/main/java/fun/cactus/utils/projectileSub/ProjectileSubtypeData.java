package fun.cactus.utils.projectileSub;

import org.bukkit.inventory.ItemStack;

public class ProjectileSubtypeData {

    private final ProjectileSubtypeType type;

    // numeric mode
    private final Integer range;
    private final Double radius;
    private final Integer wallLimit;
    private final Integer hitLimit;

    // item mode
    private final ItemStack item;

    // boolean mode
    private final Boolean flag;

    private ProjectileSubtypeData(
            ProjectileSubtypeType type,
            Integer range,
            Double radius,
            Integer wallLimit,
            Integer hitLimit,
            ItemStack item,
            Boolean flag
    ) {
        this.type = type;
        this.range = range;
        this.radius = radius;
        this.wallLimit = wallLimit;
        this.hitLimit = hitLimit;
        this.item = item;
        this.flag = flag;
    }

    public static ProjectileSubtypeData numeric(int range, double radius, int wallLimit, int hitLimit) {
        return new ProjectileSubtypeData(ProjectileSubtypeType.NUMERIC,
                range, radius, wallLimit, hitLimit, null, null);
    }

    public static ProjectileSubtypeData item(ItemStack item) {
        return new ProjectileSubtypeData(ProjectileSubtypeType.ITEM,
                null, null, null, null, item, null);
    }

    public static ProjectileSubtypeData bool(boolean flag) {
        return new ProjectileSubtypeData(ProjectileSubtypeType.BOOLEAN,
                null, null, null, null, null, flag);
    }

    public ProjectileSubtypeType getType() {
        return type;
    }

    public Integer getRange() {
        return range;
    }

    public Double getRadius() {
        return radius;
    }

    public Integer getWallLimit() {
        return wallLimit;
    }

    public Integer getHitLimit() {
        return hitLimit;
    }

    public ItemStack getItem() {
        return item;
    }

    public Boolean getFlag() {
        return flag;
    }
}
