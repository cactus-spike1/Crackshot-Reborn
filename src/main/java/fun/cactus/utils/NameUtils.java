package fun.cactus.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class NameUtils {
    private NameUtils(){}

    // Преобразует окрашенное имя предмета в нормализованную форму для сопоставления с parent_node.
    public static String toDisplayForm(String itemName) {
        ItemMeta meta = (new ItemStack(Material.DIRT)).getItemMeta();
        meta.setDisplayName(itemName);
        return meta.getDisplayName();
    }

    public static String getPureName(String itemName) {
        int nameLength = itemName.length() - 1;
        int lastIndex = itemName.lastIndexOf("§");
        if (lastIndex != -1 && lastIndex + 2 <= nameLength) {
            itemName = itemName.substring(0, lastIndex + 2);
        }

        return toDisplayForm(itemName);
    }
    public static String normalizeWeaponName(String rawName) {
        String name = ChatColor.translateAlternateColorCodes('&', rawName);
        String lastColors = ChatColor.getLastColors(name);

        return toDisplayForm(
                lastColors.isEmpty()
                        ? ChatColor.WHITE + name + ChatColor.WHITE
                        : name + lastColors
        );
    }
}
