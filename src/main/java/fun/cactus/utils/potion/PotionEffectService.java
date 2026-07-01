package fun.cactus.utils.potion;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectService {

    public static void apply(
            LivingEntity entity,
            PotionEffectData effect
    ) {

        PotionEffectType type =
                effect.getType();

        int duration =
                effect.getDuration();

        if (type.getDurationModifier() != 1.0D) {

            duration = (int) (
                    duration *
                    (1.0D / type.getDurationModifier())
            );
        }

        entity.removePotionEffect(type);

        entity.addPotionEffect(
                type.createEffect(
                        duration,
                        effect.getAmplifier()
                )
        );
    }
}
