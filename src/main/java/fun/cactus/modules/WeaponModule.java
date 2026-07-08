package fun.cactus.modules;

/**
 * Контейнер всех модулей для одного оружия
 */
public class WeaponModule {
    
    private String weaponName;
    private AmmoModule ammoModule;
    private ReloadModule reloadModule;
    private ShootingModule shootingModule;
    private FirearmActionModule firearmActionModule;
    private AttachmentsModule attachmentsModule;
    private DualWieldModule dualWieldModule;
    
    public WeaponModule(String weaponName) {
        this.weaponName = weaponName;
    }

    // Getters и Setters
    
    public String getWeaponName() {
        return weaponName;
    }

    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }

    public AmmoModule getAmmoModule() {
        return ammoModule;
    }

    public void setAmmoModule(AmmoModule ammoModule) {
        this.ammoModule = ammoModule;
    }

    public ReloadModule getReloadModule() {
        return reloadModule;
    }

    public void setReloadModule(ReloadModule reloadModule) {
        this.reloadModule = reloadModule;
    }

    public ShootingModule getShootingModule() {
        return shootingModule;
    }

    public void setShootingModule(ShootingModule shootingModule) {
        this.shootingModule = shootingModule;
    }

    public FirearmActionModule getFirearmActionModule() {
        return firearmActionModule;
    }

    public void setFirearmActionModule(FirearmActionModule firearmActionModule) {
        this.firearmActionModule = firearmActionModule;
    }

    public AttachmentsModule getAttachmentsModule() {
        return attachmentsModule;
    }

    public void setAttachmentsModule(AttachmentsModule attachmentsModule) {
        this.attachmentsModule = attachmentsModule;
    }

    public DualWieldModule getDualWieldModule() {
        return dualWieldModule;
    }

    public void setDualWieldModule(DualWieldModule dualWieldModule) {
        this.dualWieldModule = dualWieldModule;
    }

    /**
     * Валидирует все модули оружия
     */
    public boolean validateAllModules() {
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

    /**
     * Возвращает строковое представление оружия с информацией о модулях
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WeaponModule{").append("weaponName='").append(weaponName).append('\'');
        
        if (ammoModule != null && ammoModule.isEnabled()) {
            sb.append(", ammo=enabled");
        }
        
        if (reloadModule != null && reloadModule.isEnabled()) {
            sb.append(", reload=enabled");
        }
        
        if (shootingModule != null && !shootingModule.isDisabled()) {
            sb.append(", shooting=enabled");
        }
        
        if (firearmActionModule != null && firearmActionModule.getType() != null) {
            sb.append(", action=").append(firearmActionModule.getType());
        }
        
        sb.append('}');
        return sb.toString();
    }
}
