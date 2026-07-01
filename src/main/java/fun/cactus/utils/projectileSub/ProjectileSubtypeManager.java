package fun.cactus.utils.projectileSub;

import java.util.HashMap;
import java.util.Map;

public class ProjectileSubtypeManager {

    private final Map<String, ProjectileSubtypeData> dataMap = new HashMap<>();

    public void register(String weapon, ProjectileSubtypeData data) {
        dataMap.put(weapon, data);
    }

    public ProjectileSubtypeData get(String weapon) {
        return dataMap.get(weapon);
    }
}
