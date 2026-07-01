package fun.cactus.utils.particle;

import com.shampaggon.crackshot.CSDirector;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

public class ParticleEffectExecutor {
    public static void play(
            World world,
            Location location,
            ParticleEffectData effect,
            String weaponName
    ) {
        CSDirector plugin = CSDirector.getInstance(); // времено только для логов
        switch (effect.getType()) {

            case "smoke" -> {
                for (int i = 0; i < 8; i++) {
                    world.playEffect(
                            location,
                            Effect.SMOKE,
                            i
                    );
                }
            }

            case "lightning" ->
                    world.strikeLightningEffect(location);

            case "explosion" ->
                    world.createExplosion(location, 0F);

            case "potion_splash" ->
                    world.playEffect(
                            location,
                            Effect.POTION_BREAK,
                            effect.getData()
                    );

            case "block_break" -> {

                int blockId = effect.getData();

                if (blockId >= 256) {

                    plugin.printM(
                            "'" + blockId +
                            "' is not a valid block id for weapon '" +
                            weaponName + "'"
                    );

                    return;
                }

                world.playEffect(
                        location,
                        Effect.STEP_SOUND,
                        blockId
                );
            }

            case "flames" ->
                    world.playEffect(
                            location,
                            Effect.MOBSPAWNER_FLAMES,
                            effect.getData()
                    );

            default ->
                    plugin.printM(
                            "Unknown particle effect '" +
                            effect.getType() +
                            "'"
                    );
        }
    }
}
