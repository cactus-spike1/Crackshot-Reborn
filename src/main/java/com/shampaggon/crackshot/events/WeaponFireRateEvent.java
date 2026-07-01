package com.shampaggon.crackshot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Событие изменения скорострельности оружия перед выстрелом.
 */
public class WeaponFireRateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String weaponTitle;
    private ItemStack item;
    private int fireRate;

    public WeaponFireRateEvent(Player player, String weaponTitle, ItemStack item, int fireRate) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.fireRate = fireRate;
    }

    public int getFireRate() {
        return this.fireRate;
    }

    // Диапазон ограничен внутренней логикой FireProjectile, чтобы не ломать тайминги.
    public void setFireRate(int fireRate) {
        if (fireRate <= 0 || fireRate > 16) {
            throw new IllegalArgumentException("Fire rate not in range [1..16]: " + fireRate);
        }
        this.fireRate = fireRate;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
