package fun.cactus.utils.sound;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReloadTaskManager {

    private final Map<String, Collection<Integer>> reloadTasks =
            new ConcurrentHashMap<>();

    public void addTask(String playerName, int taskId) {

        reloadTasks
                .computeIfAbsent(playerName, k -> new ArrayList<>())
                .add(taskId);
    }

    public Collection<Integer> getTasks(String playerName) {
        return reloadTasks.getOrDefault(playerName, Collections.emptyList());
    }

    public void removePlayer(String playerName) {
        reloadTasks.remove(playerName);
    }
}
