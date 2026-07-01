package fun.cactus.utils.enchantment;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentCheckParser {

    public static EnchantmentCheck parse(String value) {

        if (value == null) {
            return null;
        }

        String[] parts = value.split("-");

        if (parts.length != 2) {
            return null;
        }

        Enchantment enchantment = Enchantment.getByName(parts[0]);

        if (enchantment == null) {
            return null;
        }

        try {
            int level = Integer.parseInt(parts[1]);
            return new EnchantmentCheck(enchantment, level);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}