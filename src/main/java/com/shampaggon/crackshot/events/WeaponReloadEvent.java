package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие запуска перезарядки с возможностью поменять длительность и звуки.
 */
public class WeaponReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private String soundsReload;
    private double reloadSpeed = 1.0d;
    private int reloadDuration;

    public WeaponReloadEvent(Player player, String weaponTitle, String reloadSounds, int reloadDuration) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.soundsReload = reloadSounds;
        this.reloadDuration = reloadDuration;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getSounds() {
        return this.soundsReload;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public int getReloadDuration() {
        return this.reloadDuration;
    }

    public double getReloadSpeed() {
        return this.reloadSpeed;
    }

    // Скорость перезарядки не должна уходить в отрицательные значения.
    public void setReloadSpeed(double reloadSpeed) {
        if (reloadSpeed < 0.0d) {
            reloadSpeed = 0.0d;
        }
        this.reloadSpeed = reloadSpeed;
    }

    public void setReloadDuration(int reloadDuration) {
        this.reloadDuration = reloadDuration;
    }

    public void setSounds(String soundsReload) {
        this.soundsReload = soundsReload;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
