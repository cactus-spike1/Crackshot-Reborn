package fun.cactus.utils.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WeaponCommandManager {

    private final CommandStorage storage = new CommandStorage();
    private final CommandParser parser = new CommandParser();

    public void runCommand(Player player, String parentNode) {
        runCommand(player, parentNode, player.getName(), "", "");
    }

    public void runCommand(Player player, String weaponTitle, String victim, String flightTime, String totalDamage) {
        String commands = storage.getCommands(weaponTitle + ".Extras.Run_Command");
        if (commands == null || commands.isBlank()) return;

        for (String raw : commands.split("่๋້")) {
            String cmd = parser.parseVariables(raw.trim(), player.getName(), victim, flightTime, totalDamage);
            if (cmd.isEmpty()) continue;

            if (cmd.startsWith("@")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(1).trim());
            } else {
                player.performCommand(cmd);
            }
        }
    }
}
