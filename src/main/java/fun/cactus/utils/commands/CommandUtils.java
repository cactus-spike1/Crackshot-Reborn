package fun.cactus.utils.commands;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class CommandUtils {
    private CommandUtils(){}

    // Исполняет команды из конфига, подставляя shooter/victim/flight-time и прочие переменные.
    public static void executeCommands(LivingEntity player, String parentNode, String childNode, String shooterName, String vicName, String flightTime, String totalDmg, boolean console) {
        String[] commandList = ConfigCache.getString(parentNode + childNode).split("\\|");

        for (String cmd : commandList) {
            String parsed = variableParser(cmd.trim(), shooterName, vicName, flightTime, totalDmg);
            if (parsed.isEmpty()) continue;
            if (console) {
                getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), variableParser(cmd, shooterName, vicName, flightTime, totalDmg));
            } else {
                ((Player) player).performCommand(variableParser(cmd, shooterName, vicName, flightTime, totalDmg));
            }
        }

    }

    public static String variableParser(String filter, String shooter, String victim, String flightTime, String totalDmg) {
        filter = filter.replaceAll("<shooter>", shooter).replaceAll("<victim>", victim).replaceAll("<damage>", totalDmg).replaceAll("<flight>", flightTime);
        return filter;
    }

    // Отправляет игроку локализованное сообщение с подстановкой динамических значений.
    public static void sendPlayerMessage(LivingEntity player, String parentNode, String childNode, String shooterName, String vicName, String flightTime, String totalDmg) {
        String message = ConfigCache.getString(parentNode + childNode);
        if (message != null) {
            if (player instanceof Player) {
                player.sendMessage(variableParser(message, shooterName, vicName, flightTime, totalDmg));
            }

        }
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
