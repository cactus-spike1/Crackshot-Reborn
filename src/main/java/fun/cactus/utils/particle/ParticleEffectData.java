package fun.cactus.utils.particle;

public class ParticleEffectData {
    private final String type;
    private final Integer data;

    public ParticleEffectData(String type, Integer data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Integer getData() {
        return data;
    }
}
