package fun.cactus.utils.commands;

public class CommandParser {

    public String parseVariables(String command,
                                 String shooterName,
                                 String victimName,
                                 String flightTime,
                                 String totalDamage) {

        return command
                .replace("<shooter>", shooterName)
                .replace("<victim>", victimName)
                .replace("<flightTime>", flightTime)
                .replace("<totalDamage>", totalDamage);
    }
}