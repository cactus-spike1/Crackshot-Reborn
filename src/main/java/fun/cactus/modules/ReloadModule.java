package fun.cactus.modules;

import fun.cactus.utils.sound.SoundEffect;

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
    SoundEffect[] soundsOutOfAmmo;
    SoundEffect[] soundsReloading;
    DualWieldModule dualWield;
}
