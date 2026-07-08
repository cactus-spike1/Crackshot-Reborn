package fun.cactus.utils.sound;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class SoundUtils {
    private SoundUtils(){}

    // Обёртка над playSoundEffects для случаев, где громкость надо масштабировать внешним коэффициентом.
    public static void playSoundEffectsScaled(final Entity player, String parentNode, String childNode, boolean reload, double scale, String... customSounds) {

        String soundList = customSounds.length == 0
                ? ConfigCache.getString(parentNode + childNode)
                : customSounds[0];

        getPlugin().getSoundEffectManager().playSounds(
                player,
                null,
                soundList,
                reload,
                scale,
                parentNode
        );
    }

    // Единая точка воспроизведения звуков оружия, перезарядки и взрывов с поддержкой legacy-алиасов.
    public static void playSoundEffects(final Entity player,
                                        String parentNode,
                                        String childNode,
                                        boolean reload,
                                        final Location givenCoord,
                                        String... customSounds) {

        String soundList = customSounds.length == 0
                ? ConfigCache.getString(parentNode + childNode)
                : customSounds[0];

        if (soundList == null || soundList.isEmpty()) {
            return;
        }

        getPlugin().getSoundEffectManager().playSounds(
                player,
                givenCoord,
                soundList,
                reload
        );
    }
    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
