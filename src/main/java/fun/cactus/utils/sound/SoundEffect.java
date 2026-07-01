package fun.cactus.utils.sound;


import org.bukkit.Sound;

public class SoundEffect {

    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final long delay;

    public SoundEffect(Sound sound, float volume, float pitch, long delay) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.delay = delay;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public long getDelay() {
        return delay;
    }
}