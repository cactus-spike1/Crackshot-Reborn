package fun.cactus.utils.commands;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandExecutor {

    private static final String DELIMITER = "่๋້";


    public static void execute(Player player, String commands) {
    if (commands == null || commands.isBlank()) {
        Bukkit.getLogger().warning("Commands string is empty");
        return;
    }

    Bukkit.getLogger().info("Raw commands: " + commands);

    for (String command : commands.split(DELIMITER)) {
        command = command.trim();

        if (command.isEmpty()) {
            Bukkit.getLogger().warning("Skipped empty command");
            continue;
        }

        Bukkit.getLogger().info("Executing command: " + command);

        if (command.startsWith("@")) {
            String consoleCommand = command.substring(1).trim();
            Bukkit.getLogger().info("As console: " + consoleCommand);

            if (!consoleCommand.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
            } else {
                Bukkit.getLogger().warning("Skipped empty console command");
            }
        } else {
            Bukkit.getLogger().info("As player: " + player.getName() + " -> " + command);
            player.performCommand(command);
        }
    }
}
}
