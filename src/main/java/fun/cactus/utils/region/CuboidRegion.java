package fun.cactus.utils.region;


import org.bukkit.Location;
import org.bukkit.World;

public class CuboidRegion {

    private final World world;
    private final Location pos1;
    private final Location pos2;
    private final boolean blackList;

    public CuboidRegion(World world, Location pos1, Location pos2, boolean blackList) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.blackList = blackList;
    }

    public World getWorld() {
        return world;
    }

    public boolean isBlackList() {
        return blackList;
    }

    public boolean contains(Location location) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());

        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());

        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return location.getBlockX() >= minX
                && location.getBlockX() <= maxX
                && location.getBlockY() >= minY
                && location.getBlockY() <= maxY
                && location.getBlockZ() >= minZ
                && location.getBlockZ() <= maxZ;
    }
}