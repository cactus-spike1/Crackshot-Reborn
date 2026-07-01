package fun.cactus.utils.potion;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectData {

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;

    public PotionEffectData(
            PotionEffectType type,
            int duration,
            int amplifier
    ) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public PotionEffectType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }
}