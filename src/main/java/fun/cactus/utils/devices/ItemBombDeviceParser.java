package fun.cactus.utils.devices;

import com.shampaggon.crackshot.CSMinion;

public class ItemBombDeviceParser {

    private final CSMinion csMinion;

    public ItemBombDeviceParser(CSMinion csMinion) {
        this.csMinion = csMinion;
    }

    public ItemBombDeviceInfo parse(String value) {

        String[] args = value.split(",");

        return new ItemBombDeviceInfo(
                Integer.parseInt(args[0]),
                Double.parseDouble(args[1]) * 0.1,
                csMinion.parseItemStack(args[2]),
                csMinion.parseItemStack(args[3])
        );
    }
}