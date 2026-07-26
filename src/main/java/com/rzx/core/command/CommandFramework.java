package com.rzx.core.command;

import com.rzx.core.lang.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Reflection-based command framework. Scans handler objects for {@link Command}, {@link SubCommand},
 * and {@link TabComplete} annotations and registers them directly to Bukkit's internal CommandMap.
 * No {@code plugin.yml} entries needed.
 */
public class CommandFramework {

    private final Plugin plugin;
    private final MessageManager messageManager;
    private final Map<String, Map<String, Method>> subCommands = new HashMap<>();
    private final Map<String, Method> tabCompleters = new HashMap<>();
    private final Map<String, Object> handlers = new HashMap<>();

    public CommandFramework(Plugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    /**
     * Scans the handler for annotated methods and registers them.
     *
     * @param handler the object containing annotated command methods
     */
    public void registerCommands(Object handler) {
        Method[] methods = handler.getClass().getDeclaredMethods();

        // First pass: index subcommands and tab completers
        for (Method method : methods) {
            if (method.isAnnotationPresent(SubCommand.class)) {
                SubCommand sub = method.getAnnotation(SubCommand.class);
                subCommands.computeIfAbsent(sub.parent().toLowerCase(), k -> new HashMap<>())
                        .put(sub.name().toLowerCase(), method);
            } else if (method.isAnnotationPresent(TabComplete.class)) {
                TabComplete tab = method.getAnnotation(TabComplete.class);
                tabCompleters.put(tab.name().toLowerCase(), method);
            }
        }

        // Second pass: register main commands
        for (Method method : methods) {
            if (method.isAnnotationPresent(Command.class)) {
                Command cmd = method.getAnnotation(Command.class);
                handlers.put(cmd.name().toLowerCase(), handler);
                registerBukkitCommand(cmd, method, handler);
            }
        }
    }

    private void registerBukkitCommand(Command cmdInfo, Method method, Object handler) {
        String name = cmdInfo.name();
        String permission = cmdInfo.permission();
        boolean playerOnly = cmdInfo.playerOnly();

        org.bukkit.command.Command bukkitCommand = new org.bukkit.command.Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if (playerOnly && !(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getMessage("player-only"));
                    return true;
                }

                if (!permission.isEmpty() && !sender.hasPermission(permission)) {
                    sender.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }

                // Subcommand delegation
                if (args.length > 0) {
                    Map<String, Method> subs = subCommands.get(name.toLowerCase());
                    if (subs != null) {
                        String subName = args[0].toLowerCase();
                        Method subMethod = subs.get(subName);

                        if (subMethod != null) {
                            SubCommand subInfo = subMethod.getAnnotation(SubCommand.class);

                            if (subInfo.playerOnly() && !(sender instanceof Player)) {
                                sender.sendMessage(messageManager.getMessage("player-only"));
                                return true;
                            }
                            if (!subInfo.permission().isEmpty() && !sender.hasPermission(subInfo.permission())) {
                                sender.sendMessage(messageManager.getMessage("no-permission"));
                                return true;
                            }

                            try {
                                subMethod.setAccessible(true);
                                // Pass remaining args (after subcommand name)
                                String[] subArgs = new String[args.length - 1];
                                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                                subMethod.invoke(handler, sender, subArgs);
                                return true;
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.SEVERE, "Error executing subcommand " + subName, e);
                                return true;
                            }
                        } else {
                            sender.sendMessage(messageManager.getMessage("unknown-command", "command", commandLabel));
                            return true;
                        }
                    }
                }

                // No subcommand matched or no subcommands registered â€” invoke main handler
                try {
                    method.setAccessible(true);
                    method.invoke(handler, sender, args);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error executing command " + name, e);
                }
                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                Method tabMethod = tabCompleters.get(name.toLowerCase());
                if (tabMethod != null) {
                    try {
                        tabMethod.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<String> completions = (List<String>) tabMethod.invoke(
                                handlers.get(name.toLowerCase()), sender, args);
                        return completions != null ? completions : Collections.emptyList();
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error executing tab completer for " + name, e);
                    }
                }
                return super.tabComplete(sender, alias, args);
            }
        };

        // Set aliases if provided
        if (cmdInfo.aliases().length > 0) {
            bukkitCommand.setAliases(List.of(cmdInfo.aliases()));
        }
        if (!cmdInfo.description().isEmpty()) {
            bukkitCommand.setDescription(cmdInfo.description());
        }
        if (!cmdInfo.usage().isEmpty()) {
            bukkitCommand.setUsage(cmdInfo.usage());
        }

        // Reflective injection into Bukkit CommandMap
        try {
            CommandMap commandMap = Bukkit.getCommandMap();
            commandMap.register(plugin.getName().toLowerCase(), bukkitCommand);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command " + name, e);
        }
    }
}

