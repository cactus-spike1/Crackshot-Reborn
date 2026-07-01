package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие входа и выхода из режима прицеливания.
 */
public class WeaponScopeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private boolean zoomIn;
    private boolean cancelled;

    public WeaponScopeEvent(Player player, String weaponTitle, boolean zoomIn) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.zoomIn = zoomIn;
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

    public boolean isZoomIn() {
        return this.zoomIn;
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
