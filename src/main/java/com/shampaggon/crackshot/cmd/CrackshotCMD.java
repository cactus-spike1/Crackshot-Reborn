package com.shampaggon.crackshot.cmd;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSMessages;
import fun.cactus.utils.config.ConfigCache;
import fun.cactus.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Регистрация и обработка команды /crackshot со всеми алиасами.
 */
public class CrackshotCMD extends BukkitCommand {
    CSDirector plugin;

    public CrackshotCMD(@NotNull String name, CSDirector plugin) {
        super(name);
        setAliases(Arrays.asList("cs", "cra", "shot"));
        this.plugin = plugin;
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        // Разбираем подкоманды вручную, так как команда регистрируется напрямую в commandMap.
        if (args.length == 2 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("crackshot.reloadplugin")) {
                sender.sendMessage(plugin.getHeading() + "You do not have permission to do this.");
            } else {
                plugin.reloadConfig(sender);
                if (sender instanceof Player) {
                    sender.sendMessage(plugin.getHeading() + "Configuration reloaded.");
                } else {
                    plugin.printM("Configuration reloaded.");
                }

            }
            return true;
        } else if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("[CrackShot] Version: " + plugin.getVersion());
            } else {
                sender.sendMessage("§7░ §c§l------§r§c[ -§l¬§cºcrack§7shot §c]§l------");
                sender.sendMessage("§7░ §cAuthor: §7Shampaggon");
                sender.sendMessage("§7░ §cVersion: §7" + plugin.getVersion());
                sender.sendMessage("§7░ §cAliases: §7/shot, /cra, /cs");
                sender.sendMessage("§7░ §cCommands:");
                sender.sendMessage("§7░ §c- §7/shot list [all §lOR§r§7 <page>]");
                sender.sendMessage("§7░ §c- §7/shot give <user> <weapon> <#>");
                sender.sendMessage("§7░ §c- §7/shot get <weapon> <#>");
                sender.sendMessage("§7░ §c- §7/shot reload");
                sender.sendMessage("§7░ §c- §7/shot config reload");
            }
            return true;
        } else if ((args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("give")) {
            String prefix = plugin.getHeading();
            String amount = "1";
            if (!(sender instanceof Player)) {
                prefix = "[CrackShot] ";
            }

            if (args.length == 4) {
                amount = args[3];
            }

            String parent_node = plugin.csminion.identifyWeapon(args[2]);
            if (parent_node == null) {
                sender.sendMessage(prefix + "No weapon matches '" + args[2] + "'.");
                return false;
            } else if (sender instanceof Player && !sender.hasPermission("crackshot.give." + parent_node) && !sender.hasPermission("crackshot.give.all")) {
                sender.sendMessage(prefix + "You do not have permission to give this item.");
                return false;
            } else {
                Player receiver = Bukkit.getServer().getPlayer(args[1]);
                if (receiver != null) {
                    if (receiver.getInventory().firstEmpty() != -1) {
                        sender.sendMessage(prefix + "Package delivered to " + receiver.getName() + ".");
                        plugin.csminion.getWeaponCommand(receiver, parent_node, false, amount, true, false);
                        return true;
                    } else {
                        sender.sendMessage(prefix + receiver.getName() + "'s inventory is full.");
                        return false;
                    }
                } else {
                    sender.sendMessage(prefix + "No player named '" + args[1] + "' was found.");
                    return false;
                }
            }
        } else if (!(sender instanceof Player player)) {
            sender.sendMessage("[CrackShot] Invalid command.");
            return false;
        } else {
            if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("get")) {
                String amount = "1";
                if (args.length == 3) {
                    amount = args[2];
                }

                plugin.csminion.getWeaponCommand(player, args[1], true, amount, false, false);
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                String parent_node = ItemUtils.returnParentNode(player);
                if (parent_node == null) {
                    CSMessages.sendMessage(player, plugin.getHeading(), CSMessages.Message.CANNOT_RELOAD.getMessage());
                    return true;
                } else if (!player.hasPermission("crackshot.use." + parent_node) && !player.hasPermission("crackshot.use.all")) {
                    CSMessages.sendMessage(player, plugin.getHeading(), CSMessages.Message.NP_WEAPON_USE.getMessage());
                    return false;
                } else {
                    plugin.reloadAnimation(player, parent_node);
                    return true;
                }
            } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("list")) {
                if (!player.hasPermission("crackshot.list")) {
                    player.sendMessage(plugin.getHeading() + "You do not have permission to do this.");
                    return false;
                } else {
                    plugin.csminion.listWeapons(player, args);
                    return true;
                }
            } else {
                player.sendMessage(plugin.getHeading() + "Invalid command.");
                return false;
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        // 1 аргумент
        if (args.length == 1) {
            list.add("give");
            list.add("get");
            list.add("reload");
            list.add("list");
            list.add("config");
        }

        // config reload
        else if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            list.add("reload");
        }

        // give <player>
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
        }

        // give <player> <weapon>
        else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            list.addAll(getWeapons());
        }

        // get <weapon>
        else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            list.addAll(getWeapons());
        }

        // list <page/all>
        else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            list.add("all");
            int max = ConfigCache.getInt("totalPages");
            for (int i = 1; i <= max; i++) {
                list.add(String.valueOf(i));
            }
        }

        // фильтр по вводу
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

    // Таб-комплит строится по уже загруженному списку оружия из плагина.
    private java.util.List<String> getWeapons() {
        return IntStream.range(0, plugin.wlist.size())
                .mapToObj(plugin.wlist::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
