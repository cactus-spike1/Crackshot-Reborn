package fun.cactus.utils.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class RegionParser {

    public static List<CuboidRegion> parse(String regionInfo) {
        List<CuboidRegion> regions = new ArrayList<>();

        for (String region : regionInfo.split("\\|")) {

            String[] args = region.replace(" ", "").split(",");

            if (args.length != 7 && args.length != 8) {
                continue;
            }

            World world = Bukkit.getWorld(args[0]);

            if (world == null) {
                continue;
            }

            boolean blackList = args.length == 8
                    && Boolean.parseBoolean(args[7]);

            Location pos1 = new Location(
                    world,
                    Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]),
                    Double.parseDouble(args[3])
            );

            Location pos2 = new Location(
                    world,
                    Double.parseDouble(args[4]),
                    Double.parseDouble(args[5]),
                    Double.parseDouble(args[6])
            );

            regions.add(new CuboidRegion(world, pos1, pos2, blackList));
        }

        return regions;
    }
}
