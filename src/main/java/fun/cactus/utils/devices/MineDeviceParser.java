package fun.cactus.utils.devices;

import com.shampaggon.crackshot.CSMinion;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MineDeviceParser {

    private final CSMinion csMinion;

    public MineDeviceParser(CSMinion csMinion) {
        this.csMinion = csMinion;
    }

    public MineDeviceInfo parse(String value) {

        String[] args = value.split(",");

        ItemStack fuseItem =
                csMinion.parseItemStack(args[0]);

        EntityType minecartType = EntityType.MINECART;

        if (args.length >= 2) {
            minecartType =
                    EntityType.valueOf(args[1].toUpperCase());
        }

        return new MineDeviceInfo(
                fuseItem,
                minecartType
        );
    }
}
