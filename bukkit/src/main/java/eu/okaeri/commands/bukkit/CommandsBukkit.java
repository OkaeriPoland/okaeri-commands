package eu.okaeri.commands.bukkit;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.InvocationContext;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class CommandsBukkit extends CommandsAdapter {

    private final CommandMap commandMap;
    private final JavaPlugin plugin;

    public CommandsBukkit(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            Server server = plugin.getServer();
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            this.commandMap = (CommandMap) commandMapField.get(server);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            exception.printStackTrace();
            throw new RuntimeException("cannot get commandMap", exception);
        }
    }

    @Override
    public Object resolveMissingArgument(CommandContext context, CommandMeta command, Parameter param, int i) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (Player.class.isAssignableFrom(paramType) && (param.getAnnotation(Sender.class) != null)) {
            return context.get("sender", Player.class);
        }

        // TODO: console only command
        if (ConsoleCommandSender.class.isAssignableFrom(paramType)) {
            return context.get("sender", ConsoleCommandSender.class);
        }

        // other sender
        if (CommandSender.class.isAssignableFrom(paramType) && context.has("sender", CommandSender.class)) {
            return context.get("sender");
        }

        return super.resolveMissingArgument(context, command, param, i);
    }

    @Override
    public void onRegister(CommandMeta command) {

        ServiceMeta service = command.getService();
        String serviceLabel = service.getLabel();
        String servicePermission = CommandsBukkit.this.getPermission(service);

        this.commandMap.register(serviceLabel, new Command(serviceLabel) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                try {
                    return CommandsBukkit.this.executeCommand(sender, label, args, servicePermission);
                }
                catch (Exception exception) {

                    // exception originating from the core system
                    if (exception instanceof CommandException) {
                        sender.sendMessage("Exception (system): " + exception.getMessage());
                        exception.printStackTrace();
                        return true;
                    }

                    // unexpected exception
                    sender.sendMessage("Unexpected exception occured: " + exception.getMessage());
                    exception.printStackTrace();
                    return true;
                }
            }
        });

        super.onRegister(command);
    }

    private boolean executeCommand(CommandSender sender, String label, String[] args, String servicePermission) {

        OkaeriCommands core = CommandsBukkit.super.getCore();
        String fullCommand = (label + " " + String.join(" ", args)).trim();
        CommandContext commandContext = new CommandContext();
        commandContext.add("sender", sender);

        if ((servicePermission != null) && !sender.hasPermission(servicePermission)) {
            throw new CommandException("No permission " + servicePermission + "!"); // FIXME: better error handling
        }

        Optional<InvocationContext> invocationOptional = core.invocationMatch(fullCommand);
        if (!invocationOptional.isPresent()) {
            throw new CommandException("No such command!"); // FIXME: better error handling
        }

        InvocationContext invocationContext = invocationOptional.get();
        JavaPlugin plugin = CommandsBukkit.this.plugin;

        String executorPermission = CommandsBukkit.this.getPermission(invocationContext.getExecutor());
        if ((executorPermission != null) && !sender.hasPermission(executorPermission)) {
            throw new CommandException("No permission " + executorPermission + "!"); // FIXME: better error handling
        }

        if (invocationContext.getExecutor().isAsync()) {
            Runnable prepareAndExecuteAsync = () -> this.handleExecution(sender, core, invocationContext, commandContext);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, prepareAndExecuteAsync);
            return true;
        }

        this.handleExecution(sender, core, invocationContext, commandContext);
        return true;
    }

    private void handleExecution(CommandSender sender, OkaeriCommands core, InvocationContext invocationContext, CommandContext commandContext) {
        try {
            InvocationMeta invocationMeta = core.invocationPrepare(invocationContext, commandContext);
            Object result = invocationMeta.call();

            if (result instanceof BukkitResponse) {
                sender.sendMessage(((BukkitResponse) result).render());
            }
        }
        catch (InvocationTargetException | IllegalAccessException | CommandException exception) {

            // exception originating from the called method
            if (exception instanceof InvocationTargetException) {

                if (exception.getCause() instanceof CommandException) {
                    sender.sendMessage("Exception (method): " + exception.getCause().getMessage());
                    exception.printStackTrace();
                    return;
                }

                sender.sendMessage("Unexpected exception (method): " + exception.getCause().getMessage());
                exception.printStackTrace();
                return;
            }

            // exception originating from the core system
            if (exception.getCause() instanceof CommandException) {
                sender.sendMessage("Exception (system): " + exception.getMessage());
                exception.printStackTrace();
                return;
            }

            throw new RuntimeException("ThatShouldNotBePossibleException", exception);
        }
    }

    private String getPermission(ServiceMeta service) {
        Permission permission = service.getImplementor().getClass().getAnnotation(Permission.class);
        return (permission == null) ? null : permission.value();
    }

    private String getPermission(ExecutorMeta executor) {
        Permission permission = executor.getMethod().getAnnotation(Permission.class);
        return (permission == null) ? null : permission.value();
    }
}
