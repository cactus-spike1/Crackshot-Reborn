package com.shampaggon.crackshot.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие взрыва, инициированного оружием, гранатой или airstrike.
 */
public class WeaponExplodeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location location;
    private String weaponTitle;
    private boolean isSplit;
    private boolean isAirstrike;

    public WeaponExplodeEvent(Player player, Location location, String weaponTitle, boolean isSplit, boolean isAirstrike) {
        this.player = player;
        this.location = location;
        this.weaponTitle = weaponTitle;
        this.isSplit = isSplit;
        this.isAirstrike = isAirstrike;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public boolean isSplit() {
        return this.isSplit;
    }

    public boolean isAirstrike() {
        return this.isAirstrike;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
