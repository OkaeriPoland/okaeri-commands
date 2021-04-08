package eu.okaeri.commands.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.bukkit.exception.ExceptionSource;
import eu.okaeri.commands.bukkit.exception.NoPermissionException;
import eu.okaeri.commands.bukkit.exception.NoSuchCommandException;
import eu.okaeri.commands.bukkit.handler.DefaultErrorHandler;
import eu.okaeri.commands.bukkit.handler.DefaultResultHandler;
import eu.okaeri.commands.bukkit.handler.ErrorHandler;
import eu.okaeri.commands.bukkit.handler.ResultHandler;
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

    private ErrorHandler errorHandler = new DefaultErrorHandler();
    private ResultHandler resultHandler = new DefaultResultHandler();

    public static CommandsBukkit of(JavaPlugin plugin) {
        return new CommandsBukkit(plugin);
    }

    public CommandsBukkit errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public CommandsBukkit resultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        return this;
    }

    protected CommandsBukkit(JavaPlugin plugin) {
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

        Command bukkitCommand = new Command(serviceLabel) {

            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {

                CommandContext commandContext = new CommandContext();
                commandContext.add("sender", sender);

                try {
                    return CommandsBukkit.this.executeCommand(commandContext, sender, label, args, servicePermission);
                } catch (Exception exception) {
                    CommandsBukkit.this.handleError(commandContext, exception, ExceptionSource.UNKNOWN);
                    return true;
                }
            }
        };

        bukkitCommand.setAliases(service.getAliases());
        bukkitCommand.setDescription(service.getDescription());
        this.commandMap.register(serviceLabel, bukkitCommand);

        super.onRegister(command);
    }

    private boolean executeCommand(CommandContext commandContext, CommandSender sender, String label, String[] args, String servicePermission) {

        Commands core = CommandsBukkit.super.getCore();
        String fullCommand = (label + " " + String.join(" ", args)).trim();

        if ((servicePermission != null) && !sender.hasPermission(servicePermission)) {
            this.handleError(commandContext, new NoPermissionException(servicePermission), ExceptionSource.SYSTEM);
            return true;
        }

        Optional<InvocationContext> invocationOptional = core.invocationMatch(fullCommand);
        if (!invocationOptional.isPresent()) {
            this.handleError(commandContext, new NoSuchCommandException(fullCommand), ExceptionSource.SYSTEM);
            return true;
        }

        InvocationContext invocationContext = invocationOptional.get();
        JavaPlugin plugin = CommandsBukkit.this.plugin;

        String executorPermission = CommandsBukkit.this.getPermission(invocationContext.getExecutor());
        if ((executorPermission != null) && !sender.hasPermission(executorPermission)) {
            this.handleError(commandContext, new NoPermissionException(executorPermission), ExceptionSource.SYSTEM);
            return true;
        }

        if (invocationContext.getExecutor().isAsync()) {
            Runnable prepareAndExecuteAsync = () -> this.handleExecution(sender, core, invocationContext, commandContext);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, prepareAndExecuteAsync);
            return true;
        }

        this.handleExecution(sender, core, invocationContext, commandContext);
        return true;
    }

    private void handleError(CommandContext context, Throwable throwable, ExceptionSource source) {

        Object result = this.errorHandler.onError(context, throwable, source);
        if (result == null) {
            return;
        }

        CommandSender sender = context.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot dispatch error", throwable);
        }

        if (this.resultHandler.onResult(result, sender)) {
            return;
        }

        throw new RuntimeException("Unknown return type for errorHandler [allowed: BukkitResponse, String, BaseComponent]", throwable);
    }

    private void handleExecution(CommandSender sender, Commands core, InvocationContext invocationContext, CommandContext commandContext) {
        try {
            InvocationMeta invocationMeta = core.invocationPrepare(invocationContext, commandContext);
            Object result = invocationMeta.call();

            if (this.resultHandler.onResult(result, sender)) {
                return;
            }

            throw new RuntimeException("Unknown return type for excutor [allowed: BukkitResponse, String, BaseComponent]");
        }
        catch (InvocationTargetException | IllegalAccessException | CommandException exception) {

            // exception originating from the called method
            if (exception instanceof InvocationTargetException) {

                if (exception.getCause() instanceof CommandException) {
                    this.handleError(commandContext, exception.getCause(), ExceptionSource.COMMAND);
                    return;
                }

                this.handleError(commandContext, exception, ExceptionSource.COMMAND);
                return;
            }

            // exception originating from the core system
            if (exception.getCause() instanceof CommandException) {
                this.handleError(commandContext, exception.getCause(), ExceptionSource.COMMAND);
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
