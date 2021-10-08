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
import eu.okaeri.commands.handler.DefaultTextHandler;
import eu.okaeri.commands.handler.ErrorHandler;
import eu.okaeri.commands.handler.ResultHandler;
import eu.okaeri.commands.handler.TextHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class CommandsBukkit extends CommandsAdapter {

    private final CommandMap commandMap;
    private final JavaPlugin plugin;

    private ErrorHandler errorHandler = new DefaultErrorHandler(this);
    private ResultHandler resultHandler = new DefaultResultHandler();
    private TextHandler textHandler = new DefaultTextHandler();

    public static CommandsBukkit of(@NonNull JavaPlugin plugin) {
        return new CommandsBukkit(plugin);
    }

    public CommandsBukkit errorHandler(@NonNull ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public CommandsBukkit resultHandler(@NonNull ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        return this;
    }

    public CommandsBukkit textHandler(@NonNull TextHandler textHandler) {
        this.textHandler = textHandler;
        return this;
    }

    protected CommandsBukkit(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = CommandsBukkitUnsafe.getCommandMap();
    }

    @Override
    public String resolveText(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text) {
        return this.textHandler.resolve(commandContext, invocationContext, text);
    }

    @Override
    public Object resolveMissingArgument(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull CommandMeta command, @NonNull Parameter param, int i) {

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

        return super.resolveMissingArgument(commandContext, invocationContext, command, param, i);
    }

    @Override
    public void onRegister(@NonNull CommandMeta command) {

        ServiceMeta service = command.getService();
        String serviceLabel = service.getLabel();
        String servicePermission = CommandsBukkit.this.getPermission(service);

        Command bukkitCommand = new Command(serviceLabel) {

            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {

                CommandContext commandContext = new CommandContext();
                commandContext.add("sender", sender);

                try {
                    return CommandsBukkit.this.executeCommand(command, commandContext, sender, label, args, servicePermission);
                }
                catch (Exception exception) {
                    InvocationContext dummyInvocationContext = command.newInvocationContext(label, args);
                    CommandsBukkit.this.handleError(commandContext, dummyInvocationContext, exception, ExceptionSource.UNKNOWN);
                    return true;
                }
            }
        };

        bukkitCommand.setAliases(service.getAliases());
        bukkitCommand.setDescription(service.getDescription());
        this.commandMap.register(serviceLabel, bukkitCommand);

        super.onRegister(command);
    }

    private boolean executeCommand(@NonNull CommandMeta commandMeta, @NonNull CommandContext commandContext, @NonNull CommandSender sender, @NonNull String label, @NonNull String[] args, String servicePermission) {

        Commands core = CommandsBukkit.super.getCore();
        String fullCommand = (label + " " + String.join(" ", args)).trim();

        if ((servicePermission != null) && !sender.hasPermission(servicePermission)) {
            InvocationContext dummyInvocationContext = commandMeta.newInvocationContext(label, args);
            NoPermissionException noPermissionException = new NoPermissionException(servicePermission);
            this.handleError(commandContext, dummyInvocationContext, noPermissionException, ExceptionSource.SYSTEM);
            return true;
        }

        Optional<InvocationContext> invocationOptional = core.invocationMatch(fullCommand);
        if (!invocationOptional.isPresent()) {
            InvocationContext dummyInvocationContext = commandMeta.newInvocationContext(label, args);
            NoSuchCommandException noSuchCommandException = new NoSuchCommandException(fullCommand);
            this.handleError(commandContext, dummyInvocationContext, noSuchCommandException, ExceptionSource.SYSTEM);
            return true;
        }

        InvocationContext invocationContext = invocationOptional.get();
        JavaPlugin plugin = CommandsBukkit.this.plugin;

        String executorPermission = CommandsBukkit.this.getPermission(invocationContext.getExecutor());
        if ((executorPermission != null) && !sender.hasPermission(executorPermission)) {
            NoPermissionException noPermissionException = new NoPermissionException(executorPermission);
            this.handleError(commandContext, invocationContext, noPermissionException, ExceptionSource.SYSTEM);
            return true;
        }

        if (invocationContext.isAsync()) {
            Runnable prepareAndExecuteAsync = () -> this.handleExecution(sender, core, invocationContext, commandContext);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, prepareAndExecuteAsync);
            return true;
        }

        this.handleExecution(sender, core, invocationContext, commandContext);
        return true;
    }

    private void handleError(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable throwable, @NonNull ExceptionSource source) {

        Object result = this.errorHandler.onError(commandContext, invocationContext, throwable);
        if (result == null) {
            return;
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot dispatch error", throwable);
        }

        if (this.resultHandler.onResult(result, commandContext, invocationContext)) {
            return;
        }

        throw new RuntimeException("Unknown return type for errorHandler [allowed: BukkitResponse, String, null]", throwable);
    }

    private void handleExecution(@NonNull CommandSender sender, @NonNull Commands core, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        try {
            InvocationMeta invocationMeta = core.invocationPrepare(invocationContext, commandContext);
            Object result = invocationMeta.call();

            if (this.resultHandler.onResult(result, commandContext, invocationContext)) {
                return;
            }

            if (invocationContext.getExecutor().getMethod().getReturnType() == void.class) {
                return;
            }

            throw new RuntimeException("Unknown return type for excutor [allowed: BukkitResponse, String, void]");
        }
        catch (InvocationTargetException | IllegalAccessException | CommandException exception) {

            // exception originating from the called method
            if (exception instanceof InvocationTargetException) {

                if (exception.getCause() instanceof CommandException) {
                    this.handleError(commandContext, invocationContext, exception.getCause(), ExceptionSource.COMMAND);
                    return;
                }

                this.handleError(commandContext, invocationContext, exception, ExceptionSource.COMMAND);
                return;
            }

            // exception originating from the core system (type resolver?)
            if (exception instanceof CommandException) {
                this.handleError(commandContext, invocationContext, exception, ExceptionSource.COMMAND);
                return;
            }

            // exception originating from the core system
            if (exception.getCause() instanceof CommandException) {
                this.handleError(commandContext, invocationContext, exception.getCause(), ExceptionSource.COMMAND);
                return;
            }

            throw new RuntimeException("ThatShouldNotBePossibleButSomethingHasGoneTerriblyWrongException", exception);
        }
    }

    private String getPermission(@NonNull ServiceMeta service) {
        Permission permission = service.getImplementor().getClass().getAnnotation(Permission.class);
        return (permission == null) ? null : permission.value();
    }

    private String getPermission(@NonNull ExecutorMeta executor) {
        Permission permission = executor.getMethod().getAnnotation(Permission.class);
        return (permission == null) ? null : permission.value();
    }
}
