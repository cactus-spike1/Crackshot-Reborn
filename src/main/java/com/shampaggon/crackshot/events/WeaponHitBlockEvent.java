package com.shampaggon.crackshot.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Событие попадания снаряда в блок.
 */
public class WeaponHitBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Entity objProj;
    private String weaponTitle;
    private Block hitBlock;
    private Block airBlock;

    public WeaponHitBlockEvent(Player player, Entity objProj, String weaponTitle, Block hitBlock, Block airBlock) {
        this.player = player;
        this.objProj = objProj;
        this.weaponTitle = weaponTitle;
        this.hitBlock = hitBlock;
        this.airBlock = airBlock;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getProjectile() {
        return this.objProj;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public Block getBlock() {
        return this.hitBlock;
    }

    public Block getAirBlock() {
        return this.airBlock;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
