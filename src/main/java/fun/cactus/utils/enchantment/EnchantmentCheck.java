package fun.cactus.utils.enchantment;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentCheck {

    private final Enchantment enchantment;
    private final int level;

    public EnchantmentCheck(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getLevel() {
        return level;
    }
}