package fun.cactus.utils.projectileSub;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.inventory.ItemStack;

public class ProjectileSubtypeParser {

    private static ProjectileSubtypeData parseNumeric(String raw, String weapon) {

        String[] parts = raw.split("-");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid numeric format");
        }

        try {
            int range = Integer.parseInt(parts[0]);
            double radius = Double.parseDouble(parts[1]);

            int wallLimit;
            if (parts[2].equalsIgnoreCase("all")) {
                wallLimit = -1;
            } else if (parts[2].equalsIgnoreCase("none")) {
                wallLimit = 0;
            } else {
                wallLimit = Integer.parseInt(parts[2]);
            }

            int hitLimit = Integer.parseInt(parts[3]);

            return ProjectileSubtypeData.numeric(range, radius, wallLimit, hitLimit);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number in Projectile_Subtype");
        }
    }

    private static ProjectileSubtypeData parseItem(String raw, String weapon, CSDirector cs) {

        ItemStack item = cs.csminion.parseItemStack(raw);

        if (item == null) {
            return null;
        }

        return ProjectileSubtypeData.item(item);
    }

    private static ProjectileSubtypeData parseBoolean(String raw) {

        return ProjectileSubtypeData.bool(Boolean.parseBoolean(raw));
    }

    public static ProjectileSubtypeData parseProjectileSubtype(String parentNode) {
        String raw = ConfigCache.getString(parentNode + ".Shooting.Projectile_Subtype");
        if (raw == null) {
            return null;
        }

        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
            return ProjectileSubtypeParser.parseBoolean(raw);
        }

        if (raw.contains("-")) {
            return ProjectileSubtypeParser.parseNumeric(raw, parentNode);
        }

        return ProjectileSubtypeParser.parseItem(raw, parentNode, CSDirector.getInstance());
    }
}
