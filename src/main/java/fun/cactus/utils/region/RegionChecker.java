package fun.cactus.utils.region;


import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

public class RegionChecker {

    public boolean check(Entity entity, List<CuboidRegion> regions) {

        Location location = entity.getLocation();

        boolean relevant = false;
        boolean result = false;

        for (CuboidRegion region : regions) {

            if (!location.getWorld().equals(region.getWorld())) {
                continue;
            }

            relevant = true;

            boolean inside = region.contains(location);

            if (inside) {
                if (region.isBlackList()) {
                    return false;
                }

                result = true;
            } else if (region.isBlackList()) {
                result = true;
            }
        }

        return relevant ? result : true;
    }
}

