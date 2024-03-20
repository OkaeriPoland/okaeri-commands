package eu.okaeri.commands.bukkit;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.bukkit.annotation.Async;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.bukkit.annotation.Sync;
import eu.okaeri.commands.bukkit.handler.BukkitAccessHandler;
import eu.okaeri.commands.bukkit.handler.BukkitCompletionHandler;
import eu.okaeri.commands.bukkit.handler.BukkitErrorHandler;
import eu.okaeri.commands.bukkit.handler.BukkitResultHandler;
import eu.okaeri.commands.bukkit.listener.AsyncTabCompleteListener;
import eu.okaeri.commands.bukkit.listener.PlayerCommandSendListener;
import eu.okaeri.commands.bukkit.type.CommandsBukkitTypes;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class CommandsBukkit extends OkaeriCommands {

    public static final Duration PRE_INVOKE_SYNC_WARN_TIME = Duration.ofMillis(5);
    public static final Duration TOTAL_SYNC_WARN_TIME = Duration.ofMillis(10);

    private final Map<Method, Boolean> isAsyncCacheMethod = new ConcurrentHashMap<>();
    private final Map<Class<? extends CommandService>, Boolean> isAsyncCacheService = new ConcurrentHashMap<>();

    private final Map<String, List<CommandMeta>> registeredCommands = new ConcurrentHashMap<>();
    @Getter private final Map<String, ServiceMeta> registeredServices = new ConcurrentHashMap<>();

    private final CommandMap commandMap;
    private final JavaPlugin plugin;

    protected CommandsBukkit(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = CommandsBukkitUnsafe.getCommandMap();
        this.registerType(new CommandsBukkitTypes());
        this.errorHandler(new BukkitErrorHandler(this));
        this.resultHandler(new BukkitResultHandler());
        this.accessHandler(new BukkitAccessHandler(this));
        this.completionHandler(new BukkitCompletionHandler());
    }

    public static CommandsBukkit of(@NonNull JavaPlugin plugin) {
        CommandsBukkit commandsBukkit = new CommandsBukkit(plugin);
        commandsBukkit.registerListeners();
        return commandsBukkit;
    }

    @Override
    public void close() throws IOException {
        this.getRegisteredServices().values().stream()
            .flatMap(service -> Stream.concat(Stream.of(service.getLabel()), service.getAliases().stream()))
            .forEach(CommandsBukkitUnsafe::unregister);
    }

    @SuppressWarnings("unchecked")
    public void registerListeners() {
        try {
            Class<? extends Event> PlayerCommandSendEvent = (Class<? extends Event>) Class.forName("org.bukkit.event.player.PlayerCommandSendEvent");
            PlayerCommandSendListener playerCommandSendListener = new PlayerCommandSendListener(this, PlayerCommandSendEvent);
            this.plugin.getServer().getPluginManager().registerEvent(PlayerCommandSendEvent, new Listener() {
            }, EventPriority.HIGHEST, playerCommandSendListener, this.plugin, true);
        }
        catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to register PlayerCommandSendEvent listener: " + exception + " (ignore if running an older version of Minecraft)");
        }
        try {
            Class<? extends Event> AsyncTabCompleteEvent = (Class<? extends Event>) Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            AsyncTabCompleteListener asyncTabCompleteListener = new AsyncTabCompleteListener(this, AsyncTabCompleteEvent);
            this.plugin.getServer().getPluginManager().registerEvent(AsyncTabCompleteEvent, new Listener() {
            }, EventPriority.HIGHEST, asyncTabCompleteListener, this.plugin, true);
        }
        catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to register AsyncTabCompleteEvent listener: " + exception + " (ignore if running an older version of Minecraft or not Paper)");
        }
    }

    @Override
    public Object resolveMissingArgument(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (Player.class.equals(paramType) && ((param.getAnnotation(Context.class) != null) || (param.getAnnotation(Sender.class) != null))) {
            return data.get("sender", Player.class);
        }

        // TODO: console only command
        if (ConsoleCommandSender.class.equals(paramType)) {
            return data.get("sender", ConsoleCommandSender.class);
        }

        // other sender
        if (CommandSender.class.equals(paramType) && data.has("sender", CommandSender.class)) {
            return data.get("sender");
        }

        return super.resolveMissingArgument(invocation, data, command, param, index);
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

        Command bukkitCommand = new Wrapper(serviceLabel, this, service, metas);
        bukkitCommand.setAliases(service.getAliases());
        bukkitCommand.setDescription(service.getDescription());

        this.commandMap.register(serviceLabel, bukkitCommand);
    }

    /*
      @deprecated Unsafe non-stable API allowing to access okaeri-commands from Bukkit API
     */
    @Getter
    @Setter
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class Wrapper extends Command {

        private CommandsBukkit commands;
        private ServiceMeta service;
        private List<CommandMeta> metas;

        protected Wrapper(@NonNull String name, @NonNull CommandsBukkit commands, @NonNull ServiceMeta service, @NonNull List<CommandMeta> metas) {
            super(name);
            this.commands = commands;
            this.service = service;
            this.metas = metas;
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {

            CommandData data = new CommandData();
            data.add("sender", sender);

            try {
                return this.commands.executeCommand(this.service, data, sender, label, args);
            } catch (CommandException exception) {
                this.commands.handleError(data, Invocation.of(this.service, label, args), exception);
                return true;
            }
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {

            CommandData data = new CommandData();
            data.add("sender", sender);

            return this.commands.complete(this.metas,
                Invocation.of(this.service, alias, args),
                data
            );
        }
    }

    private boolean executeCommand(@NonNull ServiceMeta service, @NonNull CommandData data, @NonNull CommandSender sender, @NonNull String label, @NonNull String[] args) {

        Instant start = Instant.now(); // time match and permissions check too, who knows how fast is it
        String fullCommand = (label + " " + String.join(" ", args)).trim();
        this.getAccessHandler().checkAccess(service, Invocation.of(service, label, args), data, false);

        Optional<Invocation> invocationOptional = this.invocationMatch(fullCommand);
        if (!invocationOptional.isPresent()) {
            throw new NoSuchCommandException(fullCommand);
        }

        Invocation invocation = invocationOptional.get();
        if (invocation.getCommand() == null) {
            throw new IllegalArgumentException("Cannot use dummy context for execution: " + invocation);
        }

        ExecutorMeta executor = invocation.getCommand().getExecutor();
        this.getAccessHandler().checkAccess(executor, invocation, data);
        Duration durationToPreInvoke = Duration.between(Instant.now(), start);

        if (durationToPreInvoke.compareTo(PRE_INVOKE_SYNC_WARN_TIME) > 0) {
            this.syncTimeWarn(service, executor, fullCommand, data, durationToPreInvoke, "pre-invoke");
        }

        if (this.isAsync(invocation)) {
            Runnable prepareAndExecuteAsync = () -> this.handleExecution(sender, invocation, data);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, prepareAndExecuteAsync);
            return true;
        }

        this.handleExecution(sender, invocation, data);
        Duration durationWithInvoke = Duration.between(Instant.now(), start);

        if (durationWithInvoke.compareTo(TOTAL_SYNC_WARN_TIME) > 0) {
            this.syncTimeWarn(service, executor, fullCommand, data, durationWithInvoke, "total");
        }

        return true;
    }

    private void handleError(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable throwable) {

        Object result = this.getErrorHandler().handle(data, invocation, throwable);
        if (result == null) {
            return;
        }

        CommandSender sender = data.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot dispatch error", throwable);
        }

        if (this.getResultHandler().handle(result, data, invocation)) {
            return;
        }

        throw new RuntimeException("Unknown return value for errorHandler [allowed: BukkitResponse, CharSequence, null]", throwable);
    }

    private void handleExecution(@NonNull CommandSender sender, @NonNull Invocation invocation, @NonNull CommandData data) {
        try {
            InvocationMeta invocationMeta = this.invocationPrepare(invocation, data);
            ServiceMeta service = invocation.getService();

            if (service != null) {
                CommandService implementor = service.getImplementor();
                implementor.preInvoke(invocation, data, invocationMeta);
            }

            Object result = invocationMeta.call();
            if (this.getResultHandler().handle(result, data, invocation)) {
                return;
            }

            CommandMeta command = invocation.getCommand();
            if (command == null) {
                throw new IllegalArgumentException("Cannot use dummy context for execution: " + invocation);
            }

            if (command.getExecutor().getMethod().getReturnType() == void.class) {
                return;
            }

            throw new RuntimeException("Unknown return value for executor [allowed: BukkitResponse, CharSequence, null]");
        } catch (Exception exception) {
            // unpack exception
            Throwable cause = exception.getCause();
            int currentIteration = 0;
            while (!(cause instanceof CommandException)) {
                // did not find a cause that is CommandException
                if (cause == null) {
                    this.handleError(data, invocation, exception);
                    return;
                }
                // cyclic exception protection i guess?
                if (currentIteration >= 20) {
                    this.handleError(data, invocation, exception);
                    return;
                }
                // gotta extract that cause
                currentIteration += 1;
                cause = cause.getCause();
            }
            this.handleError(data, invocation, cause);
        }
    }

    private boolean isAsync(@NonNull Invocation invocation) {

        if ((invocation.getCommand() == null) || (invocation.getService() == null)) {
            throw new IllegalArgumentException("Cannot use dummy context: " + invocation);
        }

        Class<? extends CommandService> serviceClass = invocation.getService().getImplementor().getClass();
        boolean serviceAsync = this.isAsyncCacheService.computeIfAbsent(serviceClass, this::isAsync);

        Method executorMethod = invocation.getCommand().getExecutor().getMethod();
        Boolean methodAsync = this.isAsyncCacheMethod.computeIfAbsent(executorMethod, this::isAsync);

        // method overrides
        if (methodAsync != null) {
            return methodAsync;
        }

        // defaults
        return serviceAsync;
    }

    @Nullable
    private Boolean isAsync(@NonNull Method method) {

        Async methodAsync = method.getAnnotation(Async.class);
        Sync methodSync = method.getAnnotation(Sync.class);

        if ((methodAsync != null) && (methodSync != null)) {
            throw new RuntimeException("Cannot use @Async and @Sync annotations simultaneously: " + method);
        }

        if ((methodAsync == null) && (methodSync == null)) {
            return null;
        }

        return methodAsync != null;
    }

    private boolean isAsync(@NonNull Class<? extends CommandService> service) {

        Async serviceAsync = service.getAnnotation(Async.class);
        Sync serviceSync = service.getAnnotation(Sync.class);

        if ((serviceAsync != null) && (serviceSync != null)) {
            throw new RuntimeException("Cannot use @Async and @Sync annotations simultaneously: " + service);
        }

        return serviceAsync != null;
    }

    private void syncTimeWarn(ServiceMeta service, ExecutorMeta executor, String fullCommand, CommandData data, Duration duration, String type) {

        // my.package.MyCommand#my_method
        String implementorName = service.getImplementor().getClass().getName();
        String methodName = executor.getMethod().getName();
        String signature = implementorName + "#" + methodName;

        // (cmd: mycmd params, context={sender=CraftPlayer{name=Player1}, some=value})
        String context = "(cmd: " + fullCommand + ", context: " + data.all() + ")";

        // main thread, 11 ms
        String thread = Thread.currentThread().getName();
        String durationText = duration.toMillis() + " ms";

        // dump!
        this.plugin.getLogger().warning(signature + " " + context + " execution took " + durationText + "! [" + thread + "] [" + type + "]");
    }
}
