package fun.cactus.utils;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public final class ProjectileUtils {
    private ProjectileUtils(){}


    public static void noArcInArchery(final Projectile proj, final Vector direction) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
            if (!proj.isDead()) {
                proj.setVelocity(direction);
                noArcInArchery(proj, direction);
            }

        }, 1L);
    }

    public static void callShootEvent(Player player, Entity objProj, String weaponTitle) {
        WeaponShootEvent event = new WeaponShootEvent(player, objProj, weaponTitle);
        getPlugin().getServer().getPluginManager().callEvent(event);
    }

    // Универсальная отложенная зачистка сущностей-снарядов и связанных метаданных.
    public static void prepareTermination(final Entity proj, final boolean remove, long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            public void run() {
                if (remove) {
                    proj.remove();
                } else {
                    proj.setVelocity(proj.getVelocity().multiply((double) 0.25F));
                }

            }
        }, delay);
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
