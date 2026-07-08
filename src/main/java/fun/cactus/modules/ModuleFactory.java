package fun.cactus.modules;

import com.shampaggon.crackshot.compatibility.SoundManager;
import fun.cactus.utils.FirearmActionsType;
import fun.cactus.utils.projectile.ProjectileType;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Фабрика для создания и парсинга модулей из конфигурации
 */
public class ModuleFactory {

    /**
     * Парсит AmmoModule из конфигурации
     */
    public static AmmoModule parseAmmoModule(FileConfiguration config, String parentNode) {
        AmmoModule module = new AmmoModule();
        
        String basePath = parentNode + ".Ammo";
        module.enable = config.getBoolean(basePath + ".Enable", false);
        
        if (module.enable) {
            String ammoItemStr = config.getString(basePath + ".Ammo_Item_ID");
            try {
                module.ammoItemID = Material.valueOf(ammoItemStr != null ? ammoItemStr.toUpperCase() : "STONE");
            } catch (IllegalArgumentException e) {
                module.ammoItemID = Material.STONE;
            }
            
            module.ammoNameCheck = config.getString(basePath + ".Ammo_Name_Check", "");
            module.takeAmmoPerShot = config.getBoolean(basePath + ".Take_Ammo_Per_Shot", false);
            module.soundsShootWithNoAmmo = parseSoundEffectsList(
                config.getString(basePath + ".Sounds_Shoot_With_No_Ammo", "")
            );
        }
        
        return module;
    }

    /**
     * Парсит ReloadModule из конфигурации
     */
    public static ReloadModule parseReloadModule(FileConfiguration config, String parentNode) {
        ReloadModule module = new ReloadModule();
        
        String basePath = parentNode + ".Reload";
        module.enable = config.getBoolean(basePath + ".Enable", false);
        
        if (module.enable) {
            module.Reload_With_Mouse = config.getBoolean(basePath + ".Reload_With_Mouse", false);
            module.StartingAmount = config.getInt(basePath + ".Starting_Amount", 30);
            module.reloadAmount = config.getInt(basePath + ".Reload_Amount", 30);
            module.takeAmmoOnReload = config.getBoolean(basePath + ".Take_Ammo_On_Reload", false);
            module.takeAmmoAsMagazine = config.getBoolean(basePath + ".Take_Ammo_As_Magazine", false);
            module.reloadDuration = config.getInt(basePath + ".Reload_Duration", 20);
            module.reloadShootDelay = config.getInt(basePath + ".Reload_Shoot_Delay", 0);
            module.destroyWhenEmpty = config.getBoolean(basePath + ".Destroy_When_Empty", false);
            module.soundsOutOfAmmo = parseSoundEffectsList(
                config.getString(basePath + ".Sounds_Out_Of_Ammo", "")
            );
            module.soundsReloading = parseSoundEffectsList(
                config.getString(basePath + ".Sounds_Reloading", "")
            );
        }
        
        return module;
    }

    /**
     * Парсит ShootingModule из конфигурации
     */
    public static ShootingModule parseShootingModule(FileConfiguration config, String parentNode) {
        ShootingModule module = new ShootingModule();
        
        String basePath = parentNode + ".Shooting";
        module.disable = config.getBoolean(basePath + ".Disable", false);
        module.dualWield = config.getBoolean(basePath + ".Dual_Wield", false);
        module.rightClickToShoot = config.getBoolean(basePath + ".Right_Click_To_Shoot", false);
        module.cancelLeftClickBlockDamage = config.getBoolean(basePath + ".Cancel_Left_Click_Block_Damage", true);
        module.cancelRightClickInteractions = config.getBoolean(basePath + ".Cancel_Right_Click_Interactions", true);
        
        module.delayBetweenShots = config.getInt(basePath + ".Delay_Between_Shots", 0);
        module.recoilAmount = config.getDouble(basePath + ".Recoil_Amount", 0.0);
        module.projectileAmount = config.getInt(basePath + ".Projectile_Amount", 1);
        
        String projectileTypeStr = config.getString(basePath + ".Projectile_Type", "arrow");
        try {
            module.projectileType = ProjectileType.valueOf(projectileTypeStr.toLowerCase());
        } catch (IllegalArgumentException e) {
            module.projectileType = ProjectileType.arrow;
        }
        
        module.removeArrowsOnImpact = config.getBoolean(basePath + ".Remove_Arrows_On_Impact", false);
        module.removeBulletDrop = config.getBoolean(basePath + ".Remove_Bullet_Drop", false);
        module.removalOrDragDelay = config.getBoolean(basePath + ".Removal_Or_Drag_Delay", false);
        
        module.projectileSpeed = config.getDouble(basePath + ".Projectile_Speed", 20.0);
        module.projectileDamage = config.getDouble(basePath + ".Projectile_Damage", 5.0);
        module.projectileFlames = config.getBoolean(basePath + ".Projectile_Flames", false);
        
        module.bulletSpread = config.getDouble(basePath + ".Bullet_Spread", 0.0);
        module.resetFallDistance = config.getBoolean(basePath + ".Reset_Fall_Distance", false);
        
        module.soundsProjectile = parseSoundEffectsList(
            config.getString(basePath + ".Sounds_Projectile", "")
        );
        module.soundsShoot = parseSoundEffectsList(
            config.getString(basePath + ".Sounds_Shoot", "")
        );
        
        return module;
    }

    /**
     * Парсит FirearmActionModule из конфигурации
     */
    public static FirearmActionModule parseFirearmActionModule(FileConfiguration config, String parentNode) {
        FirearmActionModule module = new FirearmActionModule();
        
        String basePath = parentNode + ".Firearm_Action";
        
        String typeStr = config.getString(basePath + ".Type");
        if (typeStr != null) {
            try {
                module.type = FirearmActionsType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                module.type = null;
            }
        }
        
        module.openDuration = config.getInt(basePath + ".Open_Duration", 10);
        module.closeDuration = config.getInt(basePath + ".Close_Duration", 10);
        module.closeShootDelay = config.getInt(basePath + ".Close_Shoot_Delay", 0);
        module.reloadOpenDelay = config.getInt(basePath + ".Reload_Open_Delay", 0);
        module.reloadCloseDelay = config.getInt(basePath + ".Reload_Close_Delay", 0);
        
        module.soundOpen = parseSoundEffectsList(
            config.getString(basePath + ".Sound_Open", "")
        );
        module.soundClose = parseSoundEffectsList(
            config.getString(basePath + ".Sound_Close", "")
        );
        
        return module;
    }

    /**
     * Парсит SoundEffect массив из строки конфига
     * Формат: Sound-Volume-Pitch-Delay,Sound2-Volume-Pitch-Delay,...
     */
    private static SoundEffect[] parseSoundEffects(String soundString) {
        if (soundString == null || soundString.isEmpty()) {
            return new SoundEffect[0];
        }
        
        String[] soundParts = soundString.split(",");
        java.util.List<SoundEffect> effects = new java.util.ArrayList<>();
        
        for (String part : soundParts) {
            String[] parts = part.trim().split("-");
            if (parts.length >= 4) {
                try {
                    Sound sound = SoundManager.get(parts[0].toUpperCase());
                    float volume = Float.parseFloat(parts[1]);
                    float pitch = Float.parseFloat(parts[2]);
                    long delay = Long.parseLong(parts[3]);
                    
                    effects.add(new SoundEffect(sound, volume, pitch, delay));
                } catch (Exception e) {
                    // Пропускаем неправильный звук
                }
            }
        }
        
        return effects.toArray(new SoundEffect[0]);
    }
    private static List<SoundEffect> parseSoundEffectsList(String soundString) {
        return List.of(parseSoundEffects(soundString));
    }
    /**
     * Валидирует все модули
     */
    public static boolean validateModules(
            AmmoModule ammoModule,
            ReloadModule reloadModule,
            ShootingModule shootingModule,
            FirearmActionModule firearmActionModule) {
        
        boolean valid = true;
        
        if (ammoModule != null && !ammoModule.validate()) {
            valid = false;
        }
        
        if (reloadModule != null && !reloadModule.validate()) {
            valid = false;
        }
        
        if (shootingModule != null && !shootingModule.validate()) {
            valid = false;
        }
        
        if (firearmActionModule != null && !firearmActionModule.validate()) {
            valid = false;
        }
        
        return valid;
    }
}
