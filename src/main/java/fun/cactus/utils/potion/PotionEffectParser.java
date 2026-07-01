package fun.cactus.utils.potion;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectParser {

    public static PotionEffectData parse(String value) {

        String[] args = value
                .replace(" ", "")
                .split("-");

        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid potion format"
            );
        }

        PotionEffectType type =
                PotionEffectType.getByName(
                        args[0].toUpperCase()
                );

        if (type == null) {
            throw new IllegalArgumentException(
                    "Unknown potion effect"
            );
        }

        int duration =
                Integer.parseInt(args[1]);

        int amplifier =
                Integer.parseInt(args[2]) - 1;

        return new PotionEffectData(
                type,
                duration,
                amplifier
        );
    }
}
