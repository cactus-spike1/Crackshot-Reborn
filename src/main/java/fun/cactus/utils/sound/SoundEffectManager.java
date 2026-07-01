package fun.cactus.utils.sound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundEffectManager {

    private final JavaPlugin plugin;
    private final ReloadTaskManager reloadTaskManager;

    public SoundEffectManager(JavaPlugin plugin,
                              ReloadTaskManager reloadTaskManager) {

        this.plugin = plugin;
        this.reloadTaskManager = reloadTaskManager;
    }

    public int playSound(Entity player,
                         Location location,
                         SoundEffect effect) {

        return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

            if (player != null) {

                player.getWorld().playSound(
                        player.getLocation(),
                        effect.getSound(),
                        effect.getVolume(),
                        effect.getPitch()
                );

            } else if (location != null) {

                location.getWorld().playSound(
                        location,
                        effect.getSound(),
                        effect.getVolume(),
                        effect.getPitch()
                );
            }

        }, effect.getDelay());
    }

    public void playSounds(Entity player,
                           Location location,
                           String soundList,
                           boolean reload) {

        if (soundList == null || soundList.isEmpty()) {
            return;
        }

        for (String soundData : soundList.split(",")) {

            try {

                SoundEffect effect =
                        SoundEffectParser.parse(soundData);

                int taskId =
                        playSound(player, location, effect);

                if (reload && player != null) {
                    reloadTaskManager.addTask(
                            player.getName(),
                            taskId
                    );
                }

            } catch (Exception ex) {

                Bukkit.getLogger().warning(
                        "Failed to parse sound: "
                                + soundData
                                + " -> "
                                + ex.getMessage()
                );
            }
        }
    }

    public void playSounds(Entity player,
                           Location location,
                           String soundList,
                           boolean reload,
                           double scale,
                           String weaponName) {

        if (soundList == null) {
            return;
        }

        for (String soundData : soundList.replace(" ", "").split(",")) {

            try {

                SoundEffect effect =
                        SoundEffectParser.parse(soundData);

                SoundEffect scaledEffect =
                        new SoundEffect(
                                effect.getSound(),
                                effect.getVolume(),
                                effect.getPitch(),
                                (long) (effect.getDelay() * scale)
                        );

                int taskId =
                        playSound(player, location, scaledEffect);

                if (reload && player != null) {
                    reloadTaskManager.addTask(
                            player.getName(),
                            taskId
                    );
                }

            } catch (Exception ex) {

                Bukkit.getLogger().warning(
                        "'" + soundData
                                + "' of weapon '"
                                + weaponName
                                + "' contains either an invalid number or sound!"
                );
            }
        }
    }
}