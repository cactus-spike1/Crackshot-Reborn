package fun.cactus.utils.entuty;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;

public class EntityFactory {

    public  LivingEntity create(
            World world,
            Location location,
            SpawnEntityData data
    ) {

        EntityType type =
                EntityType.valueOf(normalize(data.getEntityType()));

        LivingEntity entity =
                (LivingEntity) world.spawnEntity(location, type);

        applyVariants(entity, data);

        return entity;
    }

    private String normalize(String type) {

        return switch (type.toUpperCase()) {

            case "ZOMBIE_VILLAGER" -> "ZOMBIE";
            case "WITHER_SKELETON" -> "SKELETON";
            case "TAMED_WOLF" -> "WOLF";

            default -> type.toUpperCase();
        };
    }

    private void applyVariants(
            LivingEntity entity,
            SpawnEntityData data
    ) {

        if (!data.isBaby()) {
            return;
        }

        if (entity instanceof Zombie zombie) {
            zombie.setBaby(true);
        }
        else if (entity instanceof Creeper creeper) {
            creeper.setPowered(true);
        }
        else if (entity instanceof Ageable ageable) {
            ageable.setBaby();
        }
    }
}
