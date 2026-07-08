package fun.cactus.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class MathUtils {
    private MathUtils(){}

    // Сдвигает точку вылета снаряда влево/вправо для dual wield и более естественной стрельбы.
    public static Vector determinePosition(Player player, boolean dualWield, boolean leftClick) {
        int leftOrRight = 90;
        if (dualWield && leftClick) {
            leftOrRight = -90;
        }

        double playerYaw = (double) (player.getLocation().getYaw() + 90.0F + (float) leftOrRight) * Math.PI / (double) 180.0F;
        double x = Math.cos(playerYaw);
        double y = Math.sin(playerYaw);
        return new Vector(x, 0.0F, y);
    }
    public static float findNormal(float yaw) {
        while (yaw <= -180.0F) {
            yaw += 360.0F;
        }

        while (yaw > 180.0F) {
            yaw -= 360.0F;
        }

        return yaw;
    }

    public static BlockFace getBlockDirection(float yaw) {
        yaw = findNormal(yaw);
        switch ((int) yaw) {
            case 0:
                return BlockFace.NORTH;
            case 90:
                return BlockFace.EAST;
            case 180:
                return BlockFace.SOUTH;
            case 270:
                return BlockFace.WEST;
            default:
                if (yaw >= -45.0F && yaw < 45.0F) {
                    return BlockFace.NORTH;
                } else if (yaw >= 45.0F && yaw < 135.0F) {
                    return BlockFace.EAST;
                } else {
                    return yaw >= -135.0F && yaw < -45.0F ? BlockFace.WEST : BlockFace.SOUTH;
                }
        }
    }
}
