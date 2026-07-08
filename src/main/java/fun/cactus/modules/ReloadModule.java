package fun.cactus.modules;

import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ReloadModule {
    boolean enable;
    boolean Reload_With_Mouse;
    int StartingAmount;
    int reloadAmount;
    boolean takeAmmoOnReload;
    boolean takeAmmoAsMagazine;
    int reloadDuration;
    int reloadShootDelay;
    boolean destroyWhenEmpty;
    List<SoundEffect> soundsOutOfAmmo;
    List<SoundEffect> soundsReloading;
    DualWieldModule dualWield;

    /**
     * Получает текущее количество боеприпасов из display name оружия
     */
    public int getAmmoFromDisplayName(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) {
            return 0;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Ищем число в скобках вида «number»
        int startIndex = displayName.lastIndexOf('«');
        int endIndex = displayName.lastIndexOf('»');
        
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String ammoStr = displayName.substring(startIndex + 1, endIndex);
            try {
                return Integer.parseInt(ammoStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }

    /**
     * Обновляет количество боеприпасов в display name оружия
     */
    public void setAmmoInDisplayName(ItemStack weapon, int ammo) {
        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Заменяем число в скобках
        int startIndex = displayName.lastIndexOf('«');
        int endIndex = displayName.lastIndexOf('»');
        
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String newDisplayName = displayName.substring(0, startIndex + 1) 
                                  + ammo 
                                  + displayName.substring(endIndex);
            meta.setDisplayName(newDisplayName);
            weapon.setItemMeta(meta);
        }
    }

    /**
     * Проверяет, может ли оружие быть перезаряжено
     */
    public boolean canReload(ItemStack weapon, AmmoModule ammoModule) {
        if (!enable) {
            return false;
        }
        
        int currentAmmo = getAmmoFromDisplayName(weapon);
        
        // Если боеприпасы не разрешены, можно пополнить до максимума
        if (!ammoModule.isEnabled()) {
            return currentAmmo < reloadAmount;
        }
        
        // Если боеприпасы включены, требуется брать их на перезарядку
        return takeAmmoOnReload && currentAmmo < reloadAmount;
    }

    /**
     * Валидация конфигурации модуля
     */
    public boolean validate() {
        if (enable) {
            if (reloadAmount <= 0 || StartingAmount <= 0) {
                System.out.println("[CrackShot] ReloadModule: Количество боеприпасов должно быть > 0!");
                return false;
            }
            if (reloadDuration < 0) {
                System.out.println("[CrackShot] ReloadModule: Длительность перезарядки не может быть отрицательной!");
                return false;
            }
            if (soundsReloading == null || soundsReloading.size() == 0) {
                System.out.println("[CrackShot] ReloadModule: Нет звуков перезарядки!");
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled() {
        return enable;
    }

    public int getReloadAmount() {
        return reloadAmount;
    }

    public int getStartingAmount() {
        return StartingAmount;
    }

    public int getReloadDuration() {
        return reloadDuration;
    }

    public boolean shouldTakeAmmoOnReload() {
        return takeAmmoOnReload;
    }

    public List<SoundEffect> getSoundsReloading() {
        return soundsReloading;
    }

    public List<SoundEffect> getSoundsOutOfAmmo() {
        return soundsOutOfAmmo;
    }

    public boolean isReloadWithMouse() {
        return Reload_With_Mouse;
    }

    public boolean shouldDestroyWhenEmpty() {
        return destroyWhenEmpty;
    }
}
