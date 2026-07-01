package fun.cactus.utils.devices;

import org.bukkit.inventory.ItemStack;

public class ItemBombDeviceInfo implements DeviceInfo {

    private final int maxBombs;
    private final double speed;
    private final ItemStack bombItem;
    private final ItemStack detonatorItem;

    public ItemBombDeviceInfo(
            int maxBombs,
            double speed,
            ItemStack bombItem,
            ItemStack detonatorItem
    ) {
        this.maxBombs = maxBombs;
        this.speed = speed;
        this.bombItem = bombItem;
        this.detonatorItem = detonatorItem;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public double getSpeed() {
        return speed;
    }

    public ItemStack getBombItem() {
        return bombItem;
    }

    public ItemStack getDetonatorItem() {
        return detonatorItem;
    }
}
