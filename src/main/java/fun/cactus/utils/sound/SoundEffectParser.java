package fun.cactus.utils.sound;

import com.shampaggon.crackshot.compatibility.SoundManager;
import org.bukkit.Sound;

public class SoundEffectParser {
    public static SoundEffect parse(String soundData) {

    String[] parts = soundData.replace(" ", "").split("-");

    Sound sound = SoundManager.get(parts[0].toUpperCase());

    float volume = parts.length > 1
            ? Float.parseFloat(parts[1])
            : 1.0F;

    float pitch = parts.length > 2
            ? Float.parseFloat(parts[2])
            : 1.0F;

    long delay = parts.length > 3
            ? Long.parseLong(parts[3])
            : 0L;

    return new SoundEffect(sound, volume, pitch, delay);
}
}
