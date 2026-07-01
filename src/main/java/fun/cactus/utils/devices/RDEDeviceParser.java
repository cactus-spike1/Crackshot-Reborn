package fun.cactus.utils.devices;

public class RDEDeviceParser {

    public static RDEDeviceInfo parse(String value) {

        String[] args = value.split("-");

        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid RDE format"
            );
        }

        return new RDEDeviceInfo(
                args[0],
                args[1],
                args[2]
        );
    }
}
