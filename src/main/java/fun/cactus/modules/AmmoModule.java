package fun.cactus.modules;

import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AmmoModule {
    boolean enable;
    Material ammoItemID;
    String ammoNameCheck;
    boolean takeAmmoPerShot;
    List<SoundEffect> soundsShootWithNoAmmo;

    /**
     * Проверяет, есть ли у игрока боеприпасы в инвентаре
     */
    public boolean hasAmmo(Player player, int amount) {
        if (!enable || ammoItemID == null) {
            return true;
        }
        
        ItemStack[] inventory = player.getInventory().getContents();
        int ammoCount = 0;
        
        for (ItemStack item : inventory) {
            if (item != null && item.getType() == ammoItemID) {
                ammoCount += item.getAmount();
                if (ammoCount >= amount) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Удаляет боеприпасы из инвентаря игрока
     */
    public boolean consumeAmmo(Player player, int amount) {
        if (!enable || ammoItemID == null) {
            return true;
        }
        
        if (!hasAmmo(player, amount)) {
            return false;
        }
        
        ItemStack[] inventory = player.getInventory().getContents();
        int toRemove = amount;
        
        for (ItemStack item : inventory) {
            if (item != null && item.getType() == ammoItemID && toRemove > 0) {
                if (item.getAmount() >= toRemove) {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                } else {
                    toRemove -= item.getAmount();
                    item.setAmount(0);
                }
            }
        }
        
        player.updateInventory();
        return true;
    }

    /**
     * Валидация конфигурации модуля
     */
    public boolean validate() {
        if (enable) {
            if (ammoItemID == null) {
                System.out.println("[CrackShot] AmmoModule: Ammo_Item_ID не установлен!");
                return false;
            }
            if (!takeAmmoPerShot && soundsShootWithNoAmmo.size() == 0) {
                System.out.println("[CrackShot] AmmoModule: Нет звуков при отсутствии боеприпасов!");
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled() {
        return enable;
    }

    public Material getAmmoItemID() {
        return ammoItemID;
    }

    public boolean isTakeAmmoPerShot() {
        return takeAmmoPerShot;
    }

    public List<SoundEffect> getSoundsShootWithNoAmmo() {
        return soundsShootWithNoAmmo;
    }
}
