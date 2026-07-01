package fun.cactus.utils.entuty;

public class SpawnEntityParser {
    public static SpawnEntityData parse(String value) {

        String[] args = value.replace(" ", "").split("-");

        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Invalid format: " + value
            );
        }

        return new SpawnEntityData(
                args[0],
                Boolean.parseBoolean(args[1]),
                Boolean.parseBoolean(args[2]),
                Integer.parseInt(args[3])
        );
    }
}
