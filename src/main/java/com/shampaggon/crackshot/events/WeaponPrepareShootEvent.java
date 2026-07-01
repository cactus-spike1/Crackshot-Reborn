package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Раннее событие подготовки к выстрелу до запуска основной логики.
 */
public class WeaponPrepareShootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private boolean cancelled;

    public WeaponPrepareShootEvent(Player player, String weaponTitle) {
        this.player = player;
        this.weaponTitle = weaponTitle;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
