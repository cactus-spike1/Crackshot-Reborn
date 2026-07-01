package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие перед выстрелом, где ещё можно поменять звук и разброс.
 */
public class WeaponPreShootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private String soundsShoot;
    private double bulletSpread;
    private boolean isLeftClick;
    private boolean cancelled;

    public WeaponPreShootEvent(Player player, String weaponTitle, String soundsShoot, double bulletSpread, boolean isLeftClick) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.soundsShoot = soundsShoot;
        this.bulletSpread = bulletSpread;
        this.isLeftClick = isLeftClick;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getBulletSpread() {
        return this.bulletSpread;
    }

    public String getSounds() {
        return this.soundsShoot;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isLeftClick() {
        return this.isLeftClick;
    }

    // Разброс нормализуется в положительное значение, чтобы избежать инверсии логики.
    public void setBulletSpread(double bulletSpread) {
        this.bulletSpread = Math.abs(bulletSpread);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setSounds(String soundsShoot) {
        this.soundsShoot = soundsShoot;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
