package fun.cactus.modules;

import fun.cactus.utils.projectile.ProjectileType;
import fun.cactus.utils.projectileSub.ProjectileSubtypeData;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class ShootingModule {
    boolean disable;
    boolean dualWield;
    boolean rightClickToShoot;
    boolean cancelLeftClickBlockDamage;
    boolean cancelRightClickInteractions;
    int delayBetweenShots;
    double recoilAmount;
    int projectileAmount;
    ProjectileType projectileType;
    ProjectileSubtypeData projectileSubtype;
    boolean removeArrowsOnImpact;
    boolean removeBulletDrop;
    boolean removalOrDragDelay;
    double projectileSpeed;
    double projectileDamage;
    boolean projectileFlames;
    ProjectileIncendiaryModule projectileIncendiary;
    double bulletSpread;
    boolean resetFallDistance;
    List<SoundEffect> soundsProjectile;
    List<SoundEffect>  soundsShoot;

    /**
     * Генерирует разброс пуль с учётом bulletSpread
     */
    public Vector calculateBulletSpread() {
        java.util.Random random = new java.util.Random();
        
        double[] spread = new double[3];
        for (int i = 0; i < 3; i++) {
            spread[i] = (random.nextDouble() - random.nextDouble()) * bulletSpread * 0.1;
        }
        
        return new Vector(spread[0], spread[1], spread[2]);
    }

    /**
     * Рассчитывает отдачу оружия
     */
    public Vector calculateRecoil(Player player) {
        if (recoilAmount <= 0) {
            return new Vector(0, 0, 0);
        }
        
        Vector recoilVector = player.getLocation().getDirection().multiply(-recoilAmount * 0.1);
        return recoilVector;
    }

    /**
     * Проверяет возможность выстрела
     */
    public boolean canShoot(ReloadModule reloadModule, AmmoModule ammoModule, int currentAmmo) {
        // Если стрельба отключена
        if (disable) {
            return false;
        }
        
        // Если перезарядка включена и боеприпасов нет
        if (reloadModule != null && reloadModule.isEnabled()) {
            if (currentAmmo <= 0) {
                return false;
            }
        }
        
        // Если боеприпасы включены и требуются при выстреле
        if (ammoModule != null && ammoModule.isEnabled() && ammoModule.isTakeAmmoPerShot()) {
            // Проверка будет в CSDirector
            return true;
        }
        
        return true;
    }

    /**
     * Валидация конфигурации модуля
     */
    public boolean validate() {
        if (disable) {
            return true;
        }
        
        if (projectileType == null) {
            System.out.println("[CrackShot] ShootingModule: Projectile_Type не установлен!");
            return false;
        }
        
        if (projectileAmount < 0) {
            System.out.println("[CrackShot] ShootingModule: Projectile_Amount не может быть отрицательным!");
            return false;
        }
        
        if (delayBetweenShots < 0) {
            System.out.println("[CrackShot] ShootingModule: Delay_Between_Shots не может быть отрицательным!");
            return false;
        }
        
        if (bulletSpread < 0) {
            System.out.println("[CrackShot] ShootingModule: Bullet_Spread не может быть отрицательным!");
            return false;
        }
        
        if (projectileSpeed < 0) {
            System.out.println("[CrackShot] ShootingModule: Projectile_Speed не может быть отрицательным!");
            return false;
        }
        
        return true;
    }

    public boolean isDisabled() {
        return disable;
    }

    /**
     * Вычисляет направление полёта снаряда с учётом разброса
     */
    public Vector calculateProjectileDirection(Player player, Vector bulletSpreadVector) {
        Vector direction = player.getLocation().getDirection();
        if (bulletSpreadVector != null) {
            direction = direction.add(bulletSpreadVector).normalize();
        }
        return direction;
    }

    /**
     * Получает количество боеприпасов, нужное на один выстрел
     */
    public int getAmmoPerShot() {
        // Обычно 1, но может быть и больше в конфиге
        return 1;
    }

    /**
     * Применяет отдачу к игроку с учётом способностей
     */
    public void applyRecoil(Player player, boolean noVerticalRecoil, boolean jetpackMode) {
        if (recoilAmount <= 0) {
            return;
        }
        
        Vector recoilVector = calculateRecoil(player);
        
        if (jetpackMode) {
            player.setVelocity(new Vector(0, recoilAmount, 0));
        } else {
            if (noVerticalRecoil) {
                recoilVector = recoilVector.multiply(new Vector(1, 0, 1));
            }
            player.setVelocity(recoilVector);
        }
    }

    /**
     * Получает эффективный разброс с учётом прицеливания и скрытности
     */
    public double getEffectiveBulletSpread(Player player, boolean sneakEnabled, double sneakSpread, boolean zooming, double zoomSpread) {
        double effectiveSpread = bulletSpread;
        
        if (player.isSneaking() && sneakEnabled) {
            effectiveSpread = sneakSpread;
        }
        
        if (player.hasMetadata("ironsights") && zooming) {
            effectiveSpread = zoomSpread;
        }
        
        // Минимум 0.1 чтобы не было точных выстрелов
        if (effectiveSpread == 0.0) {
            effectiveSpread = 0.1;
        }
        
        return effectiveSpread;
    }

    public boolean isDualWield() {
        return dualWield;
    }

    public boolean isRightClickToShoot() {
        return rightClickToShoot;
    }

    public int getDelayBetweenShots() {
        return delayBetweenShots;
    }

    public double getRecoilAmount() {
        return recoilAmount;
    }

    public int getProjectileAmount() {
        return projectileAmount;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public double getBulletSpread() {
        return bulletSpread;
    }

    public boolean shouldResetFallDistance() {
        return resetFallDistance;
    }

    public boolean shouldRemoveBulletDrop() {
        return removeBulletDrop;
    }

    public List<SoundEffect> getSoundsShoot() {
        return soundsShoot;
    }

    public List<SoundEffect> getSoundsProjectile() {
        return soundsProjectile;
    }

    public double getProjectileDamage() {
        return projectileDamage;
    }

    public boolean isProjectileFlames() {
        return projectileFlames;
    }

    public boolean shouldCancelLeftClickBlockDamage() {
        return cancelLeftClickBlockDamage;
    }

    public boolean shouldCancelRightClickInteractions() {
        return cancelRightClickInteractions;
    }
}
