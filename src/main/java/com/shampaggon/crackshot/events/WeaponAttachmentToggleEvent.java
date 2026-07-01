package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Событие переключения режима навесного модуля.
 */
public class WeaponAttachmentToggleEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private ItemStack item;
    private int toggleDelay;
    private boolean cancelled;

    public WeaponAttachmentToggleEvent(Player player, String weaponTitle, ItemStack item, int toggleDelay) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.toggleDelay = toggleDelay;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getToggleDelay() {
        return this.toggleDelay;
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

    public void setToggleDelay(int toggleDelay) {
        this.toggleDelay = toggleDelay;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
