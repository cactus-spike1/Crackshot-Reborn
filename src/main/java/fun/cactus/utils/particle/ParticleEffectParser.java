package fun.cactus.utils.particle;


public class ParticleEffectParser {

    public static ParticleEffectData parse(String effect) {

        String[] args = effect
                .replace(" ", "")
                .split("-");

        if (args.length == 1) {
            return new ParticleEffectData(
                    args[0].toLowerCase(),
                    null
            );
        }

        if (args.length == 2) {
            return new ParticleEffectData(
                    args[0].toLowerCase(),
                    Integer.parseInt(args[1])
            );
        }

        throw new IllegalArgumentException(
                "Invalid particle format: " + effect
        );
    }
}