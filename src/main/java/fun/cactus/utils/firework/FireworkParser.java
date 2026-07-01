package fun.cactus.utils.firework;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;

public final class FireworkParser {


    public static FireworkData parse(String value) {

        String[] args =
                value.replace(" ", "")
                        .split("-");

        if (args.length != 6) {
            throw new IllegalArgumentException(
                    "Expected format: Type-Trail-Flicker-Red-Green-Blue"
            );
        }

        FireworkEffect.Type type =
                FireworkEffect.Type.valueOf(
                        args[0].toUpperCase()
                );

        boolean trail =
                Boolean.parseBoolean(args[1]);

        boolean flicker =
                Boolean.parseBoolean(args[2]);

        int red =
                Integer.parseInt(args[3]);

        int green =
                Integer.parseInt(args[4]);

        int blue =
                Integer.parseInt(args[5]);

        return new FireworkData(
                type,
                trail,
                flicker,
                Color.fromRGB(
                        red,
                        green,
                        blue
                )
        );
    }
}
