package eu.okaeri.commands.bukkit;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.bukkit.handler.BukkitAccessHandler;
import eu.okaeri.commands.bukkit.handler.BukkitCompletionHandler;
import eu.okaeri.commands.bukkit.handler.BukkitErrorHandler;
import eu.okaeri.commands.bukkit.handler.BukkitResultHandler;
import eu.okaeri.commands.bukkit.type.CommandsBukkitTypes;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandsBukkit extends OkaeriCommands {

    private final Map<String, List<CommandMeta>> registeredCommands = new ConcurrentHashMap<>();
    private final Map<String, ServiceMeta> registeredServices = new ConcurrentHashMap<>();

    private final CommandMap commandMap;
    private final JavaPlugin plugin;

    public static CommandsBukkit of(@NonNull JavaPlugin plugin) {
        return new CommandsBukkit(plugin);
    }

    protected CommandsBukkit(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = CommandsBukkitUnsafe.getCommandMap();
        this.registerType(new CommandsBukkitTypes());
        this.errorHandler(new BukkitErrorHandler(this));
        this.resultHandler(new BukkitResultHandler());
        this.accessHandler(new BukkitAccessHandler(this));
        this.completionHandler(new BukkitCompletionHandler());
    }

    @Override
    public Object resolveMissingArgument(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (Player.class.equals(paramType) && (param.getAnnotation(Sender.class) != null)) {
            return commandContext.get("sender", Player.class);
        }

        // TODO: console only command
        if (ConsoleCommandSender.class.equals(paramType)) {
            return commandContext.get("sender", ConsoleCommandSender.class);
        }

        // other sender
        if (CommandSender.class.equals(paramType) && commandContext.has("sender", CommandSender.class)) {
            return commandContext.get("sender");
        }

        return super.resolveMissingArgument(invocationContext, commandContext, command, param, index);
    }

    @Override
    public void onRegister(@NonNull CommandMeta command) {

        ServiceMeta service = command.getService();
        String serviceLabel = service.getLabel();

        if (this.registeredServices.containsKey(serviceLabel)) {
            ServiceMeta currentService = this.registeredServices.get(serviceLabel);
            if (!service.equals(currentService)) {
                String currentName = currentService.getImplementor().getClass().getName();
                String newName = service.getImplementor().getClass().getName();
                throw new RuntimeException("Cannot override command '" + serviceLabel + "' [current: " + currentName + ", new: " + newName + "]");
            }
        }

        if (this.registeredCommands.containsKey(serviceLabel)) {
            this.registeredCommands.get(serviceLabel).add(command);
            return;
        }

        this.registeredServices.put(serviceLabel, service);
        this.registeredCommands.put(serviceLabel, new ArrayList<>());

        List<CommandMeta> metas = this.registeredCommands.get(serviceLabel);
        metas.add(command);

        Command bukkitCommand = new Command(serviceLabel) {

            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {

                CommandContext commandContext = new CommandContext();
                commandContext.add("sender", sender);

                try {
                    return CommandsBukkit.this.executeCommand(service, commandContext, sender, label, args);
                } catch (CommandException exception) {
                    CommandsBukkit.this.handleError(commandContext, InvocationContext.of(service, label, args), exception);
                    return true;
                }
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {

                CommandContext commandContext = new CommandContext();
                commandContext.add("sender", sender);

                return CommandsBukkit.this.complete(metas,
                        InvocationContext.of(service, alias, args),
                        commandContext
                );
            }
        };

        bukkitCommand.setAliases(service.getAliases());
        bukkitCommand.setDescription(service.getDescription());
        this.commandMap.register(serviceLabel, bukkitCommand);
    }

    private boolean executeCommand(@NonNull ServiceMeta service, @NonNull CommandContext commandContext, @NonNull CommandSender sender, @NonNull String label, @NonNull String[] args) {

        String fullCommand = (label + " " + String.join(" ", args)).trim();
        this.accessHandler.checkAccess(service, InvocationContext.of(service, label, args), commandContext);

        Optional<InvocationContext> invocationOptional = this.invocationMatch(fullCommand);
        if (!invocationOptional.isPresent()) {
            throw new NoSuchCommandException(fullCommand);
        }

        InvocationContext invocationContext = invocationOptional.get();
        this.accessHandler.checkAccess(invocationContext.getExecutor(), invocationContext, commandContext);

        if (invocationContext.isAsync()) {
            Runnable prepareAndExecuteAsync = () -> this.handleExecution(sender, invocationContext, commandContext);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, prepareAndExecuteAsync);
            return true;
        }

        this.handleExecution(sender, invocationContext, commandContext);
        return true;
    }

    private void handleError(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable throwable) {

        Object result = this.errorHandler.handle(commandContext, invocationContext, throwable);
        if (result == null) {
            return;
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot dispatch error", throwable);
        }

        if (this.resultHandler.handle(result, commandContext, invocationContext)) {
            return;
        }

        throw new RuntimeException("Unknown return type for errorHandler [allowed: BukkitResponse, String, null]", throwable);
    }

    private void handleExecution(@NonNull CommandSender sender, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        try {
            InvocationMeta invocationMeta = this.invocationPrepare(invocationContext, commandContext);
            Object result = invocationMeta.call();

            if (this.resultHandler.handle(result, commandContext, invocationContext)) {
                return;
            }

            if (invocationContext.getExecutor().getMethod().getReturnType() == void.class) {
                return;
            }

            throw new RuntimeException("Unknown return type for excutor [allowed: BukkitResponse, String, void]");
        } catch (CommandException exception) {
            // unpack exception
            Throwable cause = exception.getCause();
            while (!(cause instanceof CommandException)) {
                if (cause == null) {
                    this.handleError(commandContext, invocationContext, exception);
                    return;
                }
                cause = exception.getCause();
            }
            this.handleError(commandContext, invocationContext, cause);
        }
    }
}
