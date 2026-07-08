package com.shampaggon.crackshot;

import fun.cactus.utils.ItemUtils;
import fun.cactus.utils.weapon.WeaponAttachmentUtils;
import fun.cactus.utils.weapon.WeaponHelperUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Публичный utility-слой для интеграции CrackShot с другими плагинами.
 */
public class CSUtility {
    private CSDirector classOne;
    private CSMinion classTwo;

    public CSUtility() {
        this.classOne = (CSDirector) Bukkit.getServer().getPluginManager().getPlugin("CrackShot");
        if (this.classOne != null) {
            this.classTwo = this.classOne.csminion;
        }
    }

    // Проверяет, что CrackShot найден и готов к использованию из внешнего плагина.
    public boolean isAvailable() {
        return this.classOne != null && this.classTwo != null;
    }

    // Возвращает текущую версию CrackShot или null, если плагин недоступен.
    public String getVersion() {
        return this.classOne != null ? this.classOne.getVersion() : null;
    }

    // Возвращает список всех зарегистрированных weaponTitle из конфигов.
    public Collection<String> getWeaponTitles() {
        if (this.classOne == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(new ArrayList<>(new LinkedHashSet<>(this.classOne.parentlist.values())));
    }

    // Пытается разрешить weaponTitle по точному или частичному имени.
    public String resolveWeaponTitle(String weaponTitle) {
        if (this.classTwo == null || weaponTitle == null || weaponTitle.trim().isEmpty()) {
            return null;
        }

        return this.classTwo.identifyWeapon(weaponTitle);
    }

    // Проверяет, существует ли оружие с указанным названием.
    public boolean hasWeapon(String weaponTitle) {
        return this.resolveWeaponTitle(weaponTitle) != null;
    }

    // Выдаёт игроку оружие через внутреннюю логику CrackShot.
    public boolean giveWeapon(Player receiver, String weaponTitle, int amount) {
        if (receiver == null) return false;
        if (receiver.getInventory() == null) return false;
        if (receiver.getInventory().firstEmpty() == -1) return false;
        if (amount < 1) return false;

        if (this.classTwo != null) {
            String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
            if (resolvedWeapon == null) return false;

            this.classTwo.getWeaponCommand(receiver, resolvedWeapon, false, String.valueOf(amount), true, true);
            return true;
        }
        return false;
    }

    // Создаёт ItemStack оружия без помещения его в инвентарь игрока.
    public ItemStack generateWeapon(String weaponTitle) {
        if (this.classTwo == null) return null;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return null;

        return this.classTwo.vendingMachine(resolvedWeapon);
    }

    // Создаёт ItemStack оружия и сразу задаёт количество.
    public ItemStack generateWeapon(String weaponTitle, int amount) {
        if (amount < 1) return null;

        ItemStack item = this.generateWeapon(weaponTitle);
        if (item != null) {
            item.setAmount(amount);
        }
        return item;
    }

    // Запускает штатную логику взрыва для указанного оружия в заданной точке.
    public void generateExplosion(Player player, Location loc, String weaponTitle) {
        if (this.classOne == null || loc == null) return;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return;

        this.classOne.projectileExplosion(null, resolvedWeapon, false, player, false, true, null, loc.getBlock(), true, 0);
    }

    // Размещает мину через основной контроллер плагина.
    public void spawnMine(Player player, Location loc, String weaponTitle) {
        if (this.classOne == null || loc == null) return;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return;

        this.classOne.deployMine(player, resolvedWeapon, loc);
    }

    // Привязывает к снаряду владельца и название оружия, если тип снаряда поддерживается.
    public void setProjectile(Player player, Projectile proj, String weaponTitle) {
        if (player == null || proj == null || this.classOne == null) return;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return;

        EntityType projType = proj.getType();
        switch (projType) {
            case ARROW:
            case SNOWBALL:
            case FIREBALL:
            case WITHER_SKULL:
            case EGG:
                proj.setShooter(player);
                proj.setMetadata("projParentNode", new FixedMetadataValue(this.classOne, resolvedWeapon));
                break;
            default:
                // other projectiles are not handled
                break;
        }
    }

    // Проверяет, является ли предмет оружием CrackShot.
    public boolean isWeapon(ItemStack item) {
        return this.getWeaponTitle(item) != null;
    }

    // Пытается определить название оружия по ItemStack.
    public String getWeaponTitle(ItemStack item) {
        if (item == null || this.classOne == null) return null;
        String[] weaponInfo = ItemUtils.itemParentNode(item, null);
        if (weaponInfo == null || weaponInfo.length == 0) return null;
        return weaponInfo[0];
    }

    // Определяет оружие по предмету в руке игрока.
    public String getWeaponTitle(Player player) {
        if (player == null || player.getInventory() == null) return null;
        return this.getWeaponTitle(player.getItemInHand());
    }

    // Читает weaponTitle из метадаты выпущенного снаряда.
    public String getWeaponTitle(Projectile proj) {
        if (proj == null) return null;
        if (!proj.hasMetadata("projParentNode")) return null;
        MetadataValue val = proj.getMetadata("projParentNode").get(0);
        return val != null ? val.asString() : null;
    }

    // Читает weaponTitle из метадаты primed TNT, созданного CrackShot.
    public String getWeaponTitle(TNTPrimed tnt) {
        if (tnt == null) return null;
        if (!tnt.hasMetadata("CS_potex")) return null;
        MetadataValue val = tnt.getMetadata("CS_potex").get(0);
        return val != null ? val.asString() : null;
    }

    // Возвращает customModelData оружия из конфига.
    public Integer getCustomModelData(String weaponTitle) {
        if (this.classOne == null) return null;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return null;

        return this.classOne.getCustomModelData(resolvedWeapon);
    }

    // Возвращает расчётную ёмкость магазина оружия с учётом WeaponCapacityEvent.
    public int getReloadAmount(Player player, String weaponTitle, ItemStack item) {
        if (this.classOne == null) return 0;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return 0;

        return this.classOne.getReloadAmount(player, resolvedWeapon, item);
    }

    // Возвращает информацию об attachment в формате [type, activeAttachment].
    public String[] getAttachment(String weaponTitle, ItemStack item) {
        if (this.classOne == null) return null;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return null;

        return WeaponAttachmentUtils.getAttachment(resolvedWeapon, item);
    }

    // Проверяет, работает ли оружие в режиме dual wield для данного игрока и предмета.
    public boolean isDualWield(Player player, String weaponTitle, ItemStack item) {
        if (this.classOne == null) return false;

        String resolvedWeapon = this.resolveWeaponTitle(weaponTitle);
        if (resolvedWeapon == null) return false;

        return WeaponHelperUtils.isDualWield(player, resolvedWeapon, item);
    }

    // Принудительно запускает выстрел из оружия, которое игрок держит в руке.
    public boolean shootHeldWeapon(Player player, boolean leftClick) {
        if (player == null || this.classOne == null) return false;

        String weaponTitle = this.getWeaponTitle(player);
        if (weaponTitle == null) return false;

        this.classOne.fireProjectile(player, weaponTitle, leftClick);
        return true;
    }

    // Принудительно запускает перезарядку оружия, которое игрок держит в руке.
    public boolean reloadHeldWeapon(Player player) {
        if (player == null || this.classOne == null) return false;

        String weaponTitle = this.getWeaponTitle(player);
        if (weaponTitle == null) return false;

        this.classOne.reloadAnimation(player, weaponTitle);
        return true;
    }

    // Снимает режим прицеливания у игрока, если он активен.
    public void unscope(Player player) {
        if (player == null || this.classOne == null) return;
        this.classOne.unscopePlayer(player);
    }

    // Возвращает основной экземпляр плагина для прямого доступа извне.
    public CSDirector getHandle() {
        return this.classOne;
    }

    // Возвращает Bukkit Plugin-инстанс CrackShot для удобных проверок и регистрации зависимостей.
    public Plugin getPlugin() {
        return this.classOne;
    }
}
