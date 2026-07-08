package fun.cactus.utils.weapon;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDualWieldEvent;
import fun.cactus.utils.ItemUtils;
import fun.cactus.utils.NameUtils;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class WeaponHelperUtils {

    private WeaponHelperUtils(){}

    // Проверяет режим dual wield с учётом предмета в руке и конфига оружия.
    public static boolean isDualWield(Player player, String weaponTitle, ItemStack item) {

        boolean dualWield = ConfigCache.getBoolean(weaponTitle + ".Shooting.Dual_Wield");
        WeaponDualWieldEvent event = new WeaponDualWieldEvent(player, weaponTitle, item, dualWield);
        getPlugin().getServer().getPluginManager().callEvent(event);
        return event.isDualWield();
    }

    public static boolean isDifferentItem(ItemStack item, String weaponTitle) {
        if (ConfigCache.getBoolean(weaponTitle + ".Item_Information.Skip_Name_Check")) {
            String itemWeaponTitle = ItemUtils.isSkipNameItem(item);
            return itemWeaponTitle == null || !itemWeaponTitle.equals(weaponTitle);
        } else {
            String itemName = ConfigCache.getString(weaponTitle + ".Item_Information.Item_Name");
            String heldItemName = NameUtils.toDisplayForm(item.getItemMeta().getDisplayName());
            return !heldItemName.startsWith(itemName);
        }
    }

    public static boolean isAccessory(String attachmentType) {
        return attachmentType != null && attachmentType.equalsIgnoreCase("accessory");
    }

    public static boolean isAir(Material m) {
        return m == Material.AIR || m.name().endsWith("_AIR");
    }

    public static boolean isValid(int tick, int fireRate) {
        return switch (fireRate) {
            case 1 -> tick % 4 == 1;
            case 2 -> {
                tick %= 7;
                yield tick == 1 || tick == 4;
            }
            case 3 -> tick % 3 == 1;
            case 4 -> {
                tick %= 5;
                yield tick == 1 || tick == 3;
            }
            case 5 -> {
                tick %= 7;
                yield tick == 1 || tick == 3 || tick == 5;
            }
            case 6 -> tick % 2 == 1;
            case 7 -> tick == 2 || tick % 2 == 1;
            case 8 -> {
                tick %= 5;
                yield tick == 1 || tick == 2 || tick == 4;
            }
            case 9 -> {
                tick %= 6;
                yield tick != 2 && tick != 0;
            }
            case 10 -> tick % 3 != 0;
            case 11 -> tick % 4 != 0;
            case 12 -> tick % 5 != 0;
            case 13 -> tick % 6 != 0;
            case 14 -> tick % 10 != 0;
            case 15 -> tick != 20;
            case 16 -> true;
            default -> true;
        };
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
