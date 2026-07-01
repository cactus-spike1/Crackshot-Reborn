package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие механики firearm action: затвор, помпа, рычаг и т.п.
 */
public class WeaponFirearmActionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private double speed = 1.0d;
    private boolean reload;

    public WeaponFirearmActionEvent(Player player, String weaponTitle, boolean reload) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.reload = reload;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public double getSpeed() {
        return this.speed;
    }

    public boolean isReload() {
        return this.reload;
    }

    // Скорость не может быть отрицательной, иначе ломаются анимации и задержки.
    public void setSpeed(double speed) {
        if (speed < 0.0d) {
            speed = 0.0d;
        }
        this.speed = speed;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
