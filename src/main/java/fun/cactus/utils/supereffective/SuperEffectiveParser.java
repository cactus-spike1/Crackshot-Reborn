package fun.cactus.utils.supereffective;

import org.bukkit.entity.EntityType;

public final class SuperEffectiveParser {

    public static SuperEffectiveData parse(
            String value
    ) {

        String[] args =
                value.replace(" ", "")
                     .split("-");

        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid Super_Effective format"
            );
        }

        EntityType entityType =
                EntityType.valueOf(
                        args[0].toUpperCase()
                );

        double multiplier =
                Double.parseDouble(args[1]);

        return new SuperEffectiveData(
                entityType,
                multiplier
        );
    }
}