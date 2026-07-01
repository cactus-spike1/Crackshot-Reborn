package fun.cactus.utils.firework;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;

public class FireworkData {

    private final FireworkEffect.Type type;
    private final boolean trail;
    private final boolean flicker;
    private final Color color;

    public FireworkData(
            FireworkEffect.Type type,
            boolean trail,
            boolean flicker,
            Color color
    ) {
        this.type = type;
        this.trail = trail;
        this.flicker = flicker;
        this.color = color;
    }

    public FireworkEffect.Type getType() {
        return type;
    }

    public boolean isTrail() {
        return trail;
    }

    public boolean isFlicker() {
        return flicker;
    }

    public Color getColor() {
        return color;
    }
}