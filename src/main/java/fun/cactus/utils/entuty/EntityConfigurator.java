package fun.cactus.utils.entuty;

import com.shampaggon.crackshot.CSDirector;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityConfigurator {

    public static void configure(
            LivingEntity entity,
            SpawnEntityData data,
            AnimalTamer owner,
            String customName
    ) {

        if (customName != null) {
            entity.setCustomName(customName);
            entity.setCustomNameVisible(true);
        }

        if (data.isExplode()) {
            entity.setMetadata(
                    "CS_Boomer",
                    new FixedMetadataValue(CSDirector.getInstance(), true)
            );
        }

        switch (data.getEntityType().toUpperCase()) {

            case "ZOMBIE_VILLAGER" -> {
                ((Zombie) entity).setVillager(true);
            }

            case "WITHER_SKELETON" -> {
                ((Skeleton) entity)
                        .setSkeletonType(Skeleton.SkeletonType.WITHER);
            }

            case "TAMED_WOLF" -> {
                ((Wolf) entity).setOwner(owner);
            }
        }
    }
}