package fun.cactus.modules;

import fun.cactus.utils.sound.SoundEffect;

public class RiotShieldModule {
    boolean enable;
    boolean doNotBlockProjectiles;
    boolean doNotBlockMeleeAttacks;
    boolean durabilityBasedOnDamage;
    int durabilityLossPerHit;
    boolean forcefieldMode;
    boolean onlyWorksWhileBlocking;
    SoundEffect[] soundsBlocked;
    SoundEffect[] soundsBreak;
}
