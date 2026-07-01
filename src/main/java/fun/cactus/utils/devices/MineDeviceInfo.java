package fun.cactus.utils.devices;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MineDeviceInfo implements DeviceInfo {

    private final ItemStack fuseItem;
    private final EntityType minecartType;

    public MineDeviceInfo(
            ItemStack fuseItem,
            EntityType minecartType
    ) {
        this.fuseItem = fuseItem;
        this.minecartType = minecartType;
    }

    public ItemStack getFuseItem() {
        return fuseItem;
    }

    public EntityType getMinecartType() {
        return minecartType;
    }
}
