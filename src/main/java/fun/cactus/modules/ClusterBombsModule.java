package fun.cactus.modules;

import fun.cactus.utils.particle.ParticleEffectData;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.Material;

public class ClusterBombsModule {
    boolean enable;
    Material bombletType;
    int delayBeforeSplit;
    int numberOfSplits;
    int numberOfBomblets;
    int speedOfBomblets;
    int delayBeforeDetonation;
    int detonationDelayVariation;
    ParticleEffectData[] particleRelease;
    SoundEffect[] soundRelease;
}
