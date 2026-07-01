package com.shampaggon.crackshot.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие установки мины или другой взрывной ловушки.
 */
public class WeaponPlaceMineEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Entity mine;
    private String weaponTitle;

    public WeaponPlaceMineEvent(Player player, Entity mine, String weaponTitle) {
        this.player = player;
        this.mine = mine;
        this.weaponTitle = weaponTitle;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getMine() {
        return this.mine;
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
