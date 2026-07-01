package fun.cactus.utils.enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentCheckManager {

    private final Map<String, EnchantmentCheck> enchants = new HashMap<>();

    public void register(String weapon, EnchantmentCheck check) {

        if (check == null) {
            return;
        }

        enchants.put(weapon, check);
    }

    public EnchantmentCheck get(String weapon) {
        return enchants.get(weapon);
    }

    public Map<String, EnchantmentCheck> getAll() {
        return enchants;
    }
}