package fun.cactus.utils.config;

import com.shampaggon.crackshot.CSDirector;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigCacheUtils {
    private ConfigCacheUtils(){}
    private static final String[] DIRECT_STRING_NODES = {
            ".Item_Information.Item_Type",
            ".Ammo.Ammo_Item_ID",
            ".Shooting.Projectile_Subtype",
            ".Crafting.Ingredients",
            ".Explosive_Devices.Device_Info",
            ".Airstrikes.Block_Type",
            ".Cluster_Bombs.Bomblet_Type",
            ".Shrapnel.Block_Type",
            ".Explosions.Damage_Multiplier"
    };
    private static final String[] DIRECT_DOUBLE_NODES = {
            ".Shooting.Bullet_Spread",
            ".Sneak.Bullet_Spread",
            ".Scope.Zoom_Bullet_Spread"
    };

    public static void cacheDirectWeaponValues(FileConfiguration config, String parentNode) {
        for (String path : DIRECT_STRING_NODES) {
            getPlugin().strings.put(parentNode + path, config.getString(parentNode + path));
        }

        for (String path : DIRECT_DOUBLE_NODES) {
            getPlugin().dubs.put(parentNode + path, config.getDouble(parentNode + path));
        }
    }

    public static void cachePrimitiveValues(FileConfiguration config) {
        for (String key : config.getKeys(true)) {
            Object obj = config.get(key);
            if (obj instanceof Boolean) {
                getPlugin().bools.put(key, (Boolean) obj);
            } else if (obj instanceof Integer) {
                getPlugin().ints.put(key, (Integer) obj);
            } else if (obj instanceof String) {
                getPlugin().strings.put(key, ((String) obj).replaceAll("&", "§"));
            }
        }
    }


    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
