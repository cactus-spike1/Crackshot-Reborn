package com.shampaggon.crackshot;

import org.bukkit.entity.Player;

/**
 * Снимок состояния игрока в момент выброса предмета.
 */
public class DropAttempt {
    public int itemSlot;
    public long worldTicks;

    public DropAttempt(Player player) {
        this.itemSlot = getItemSlot(player);
        this.worldTicks = getWorldTicks(player);
    }

    // Нужен для отсечения повторной обработки того же выброса в тот же тик.
    public boolean IsOnSameSlotAndTick(Player player) {
        return this.itemSlot == getItemSlot(player) && this.worldTicks == getWorldTicks(player);
    }

    private static int getItemSlot(Player player) {
        return player.getInventory().getHeldItemSlot();
    }

    private static long getWorldTicks(Player player) {
        return player.getWorld().getFullTime();
    }
}
