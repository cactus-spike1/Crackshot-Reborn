package fun.cactus.modules;

import fun.cactus.utils.particle.ParticleEffectData;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.Material;

public class AirstrikesModule {
    boolean enable;
    int flareActivationDelay;
    ParticleEffectData[] particleCallAirstrike;
    String messageCallAirstrike;
    Material blockType;
    int area;
    int distanceBetweenBombs;
    int heightDropped;
    int verticalVariation;
    int horizontalVariation;
    MultipleStrikesModule multipleStrikes;
    SoundEffect[] soundsAirstrike;
}
