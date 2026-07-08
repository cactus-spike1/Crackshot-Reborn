package fun.cactus.utils.config;

import com.shampaggon.crackshot.CSDirector;

public final class ConfigCache {
    private ConfigCache(){}

    public static double getDouble(String nodes) {
        Double result = getPlugin().dubs.get(nodes);
        return result != null ? result : (double) 0.0F;
    }

    public static double getConfigDouble(String nodes, double defaultValue) {
        if (getPlugin().weaponConfig != null && getPlugin().weaponConfig.isDouble(nodes)) {
            return getPlugin().weaponConfig.getDouble(nodes);
        }
        if (getPlugin().weaponConfig != null && getPlugin().weaponConfig.isInt(nodes)) {
            return getPlugin().weaponConfig.getInt(nodes);
        }

        return defaultValue;
    }

    public static boolean getBoolean(String nodes) {
        Boolean result = getPlugin().bools.get(nodes);
        return result != null ? result : false;
    }

    public static int getInt(String nodes) {
        Integer result = getPlugin().ints.get(nodes);
        return result != null ? result : 0;
    }

    public static String getString(String nodes) {
        String result = getPlugin().strings.get(nodes);
        return result != null ? result : null;
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
