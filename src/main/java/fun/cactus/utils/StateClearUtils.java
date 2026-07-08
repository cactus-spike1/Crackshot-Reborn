package fun.cactus.utils;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Map;

public final class StateClearUtils {
    private StateClearUtils(){}

    public static void clearTransientState() {
        getPlugin().zoomStorage.clear();
        getPlugin().burst_task_IDs.clear();
        getPlugin().global_reload_IDs.clear();
        getPlugin().itembombs.clear();
        getPlugin().last_shot_list.clear();
        getPlugin().c4_backup.clear();
        getPlugin().delayed_reload_IDs.clear();
        getPlugin().delay_list.clear();
        getPlugin().last_drop.clear();
        getPlugin().rpm_ticks.clear();
        getPlugin().rpm_shots.clear();
    }

    public static void clearConfigurationCaches() {
        getPlugin().disWorlds = new String[]{"0"};
        getPlugin().bools.clear();
        getPlugin().ints.clear();
        getPlugin().strings.clear();
        getPlugin().dubs.clear();
        getPlugin().weaponModules.clear();
        getPlugin().morobust.clear();
        getPlugin().wlist.clear();
        getPlugin().rdelist.clear();
        getPlugin().boobs.clear();
        getPlugin().grouplist.clear();
        getPlugin().melees.clear();
        getPlugin().enchlist.clear();
        getPlugin().convIDs.clear();
        getPlugin().parentlist.clear();
        CSMessages.messages.clear();
    }

    public static void cleanupOnlinePlayers() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            getPlugin().removeInertReloadTag(player, 0, true);
            getPlugin().unscopePlayer(player);
            getPlugin().terminateAllBursts(player);
            getPlugin().terminateReload(player);
        }
    }

    // Удаляем подвешенные itembomb-сущности до очистки кэшей, чтобы они не переживали перезагрузку плагина.
    public static void clearTrackedItemBombs() {
        for (Map<String, ArrayDeque<Item>> subList : getPlugin().itembombs.values()) {
            for (ArrayDeque<Item> subSubList : subList.values()) {
                while (!subSubList.isEmpty()) {
                    subSubList.removeFirst().remove();
                }
            }
        }
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
