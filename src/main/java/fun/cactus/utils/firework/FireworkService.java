package fun.cactus.utils.firework;


import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;

public final class FireworkService {


    public static void spawn(
            Entity entity,
            FireworkData data
    ) {

        Location location;

        if (entity instanceof LivingEntity livingEntity) {

            location =
                    livingEntity.getEyeLocation();

        } else {

            location =
                    entity.getLocation();
        }

        Firework firework =
                entity.getWorld().spawn(
                        location,
                        Firework.class
                );

        FireworkMeta meta =
                firework.getFireworkMeta();

        FireworkEffect effect =
                FireworkEffect.builder()
                        .with(data.getType())
                        .trail(data.isTrail())
                        .flicker(data.isFlicker())
                        .withColor(data.getColor())
                        .build();

        meta.addEffect(effect);

        firework.setFireworkMeta(meta);
    }
}