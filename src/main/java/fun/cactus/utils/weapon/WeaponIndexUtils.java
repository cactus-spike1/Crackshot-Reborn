package fun.cactus.utils.weapon;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.NameUtils;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

public final class WeaponIndexUtils {
    private WeaponIndexUtils(){}

    public static void registerInventoryGroups(String parentNode) {
        String inventoryControl = ConfigCache.getString(parentNode + ".Item_Information.Inventory_Control");
        if (inventoryControl == null) {
            return;
        }

        for (String group : inventoryControl.replace(" ", "").split(",")) {
            getPlugin().grouplist.computeIfAbsent(group, key -> new HashSet<>()).add(parentNode);
        }
    }

    public static void registerWeaponName(FileConfiguration config, String parentNode, boolean accessory) {
        String name = accessory ? "§f" + parentNode : config.getString(parentNode + ".Item_Information.Item_Name");
        if (name == null) {
            getPlugin().printM("The weapon '" + parentNode + "' does not have a value for Item_Name.");
            return;
        }

        String normalizedName = NameUtils.normalizeWeaponName(name);
        String existingEntry = getPlugin().parentlist.get(normalizedName);
        if (existingEntry == null) {
            getPlugin().parentlist.put(normalizedName, parentNode);
        } else if (!accessory) {
            String nameA = config.getString(parentNode + ".Item_Information.Item_Name");
            String nameB = config.getString(existingEntry + ".Item_Information.Item_Name");
            String msg = "The item names of '" + parentNode + "' and '" + existingEntry + "' are too similar: ";
            msg = msg + "'" + nameA + "' and '" + nameB + "'. ";
            msg = msg + "Each weapon must have a unique value for Item_Name.";
            getPlugin().printM(msg);
        }

        getPlugin().strings.put(parentNode + ".Item_Information.Item_Name", normalizedName);
    }
    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
