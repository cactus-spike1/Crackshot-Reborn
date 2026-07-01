package fun.cactus.modules;

import fun.cactus.utils.projectile.ProjectileType;
import fun.cactus.utils.projectileSub.ProjectileSubtypeData;
import fun.cactus.utils.sound.SoundEffect;

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
    SoundEffect[] soundsProjectile;
    SoundEffect[] soundsShoot;
}
