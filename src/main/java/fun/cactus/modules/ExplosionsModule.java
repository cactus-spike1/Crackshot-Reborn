package fun.cactus.modules;

import fun.cactus.utils.sound.SoundEffect;

public class ExplosionsModule {
    boolean enable;
    int knockback;
    int igniteVictims;
    int damageMultiplier; // * 0.01 всегда
    /*
                        try {
                            String multiString = this.getString(parent_node + ".Explosions.Damage_Multiplier");
                            if (multiString != null) {
                                double multiplier = (double) Integer.parseInt(multiString) * 0.01;
                                totalDmg *= multiplier;
                                totalDmg = this.csminion.getSuperDamage(entVictim.getType(), parent_node, totalDmg);
                            }
                        } catch (IllegalArgumentException e) {
                        }
    */
    boolean enableFriendlyFire;
    boolean enableOwnerImmunity;
    boolean explosionNoDamage;
    Object explosionPotionEffect;//
    boolean explosionNoGrief;
    int explosionRadius;
    boolean explosionIncendiary;
    int explosionDelay;
    boolean onImpactWithAnything;
    int projectileActivationTime;
    String messageShooter;
    String messageVictim;
    SoundEffect[] soundsShooter;
    SoundEffect[] soundsVictim;
    SoundEffect[] soundsExplode;
}
