package fun.cactus.utils.devices;


public class RDEDeviceInfo implements DeviceInfo{

    private final String material;
    private final String id;
    private final String power;

    public RDEDeviceInfo(
            String material,
            String id,
            String power
    ) {
        this.material = material;
        this.id = id;
        this.power = power;
    }

    public String getMaterial() {
        return material;
    }

    public String getId() {
        return id;
    }

    public String getPower() {
        return power;
    }
}