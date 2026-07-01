package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие окончания перезарядки оружия.
 */
public class WeaponReloadCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;

    public WeaponReloadCompleteEvent(Player player, String weaponTitle) {
        this.player = player;
        this.weaponTitle = weaponTitle;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
