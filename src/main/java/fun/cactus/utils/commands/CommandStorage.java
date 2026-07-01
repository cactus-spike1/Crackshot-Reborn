package fun.cactus.utils.commands;


import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandStorage {

    private static final String DELIMITER = "่๋້";

    private final Map<String, String> strings = new HashMap<>();

    public void registerRunCommands(FileConfiguration config, String parentNode) {
        List<String> commandList = config.getStringList(parentNode + ".Extras.Run_Command");

        if (commandList.isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < commandList.size(); i++) {
            String command = commandList.get(i).trim();

            if (i > 0) {
                builder.append(DELIMITER);
            }

            if (command.startsWith("@")) {
                builder.append("@").append(command.substring(1).trim());
            } else {
                builder.append(command);
            }
        }

        strings.put(
                parentNode + ".Extras.Run_Command",
                builder.toString().replace("&", "§")
        );
    }

    public String getCommands(String path) {
        return strings.get(path);
    }
}
