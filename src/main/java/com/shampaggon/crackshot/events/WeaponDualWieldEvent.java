package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Событие определения режима dual wield для оружия.
 */
public class WeaponDualWieldEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private boolean dualWield;
    private ItemStack item;
    private Player player;
    private String weaponTitle;

    public WeaponDualWieldEvent(Player player, String weaponTitle, ItemStack item, boolean dualWield) {
        this.dualWield = dualWield;
        this.item = item;
        this.player = player;
        this.weaponTitle = weaponTitle;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getWeaponTitle() {
        return this.weaponTitle;
    }

    public boolean isDualWield() {
        return this.dualWield;
    }

    public void setDualWield(boolean dualWield) {
        this.dualWield = dualWield;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
