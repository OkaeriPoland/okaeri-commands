package eu.okaeri.commands.bungee;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.bungee.annotation.Async;
import eu.okaeri.commands.bungee.annotation.Sync;
import eu.okaeri.commands.bungee.handler.BungeeAccessHandler;
import eu.okaeri.commands.bungee.handler.BungeeCompletionHandler;
import eu.okaeri.commands.bungee.handler.BungeeErrorHandler;
import eu.okaeri.commands.bungee.handler.BungeeResultHandler;
import eu.okaeri.commands.bungee.type.CommandsBungeeTypes;
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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
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

public class CommandsBungee extends OkaeriCommands {

    public static final Duration PRE_INVOKE_SYNC_WARN_TIME = Duration.ofMillis(5);
    public static final Duration TOTAL_SYNC_WARN_TIME = Duration.ofMillis(10);

    private final Map<Method, Boolean> isAsyncCacheMethod = new ConcurrentHashMap<>();
    private final Map<Class<? extends CommandService>, Boolean> isAsyncCacheService = new ConcurrentHashMap<>();

    private final Map<String, List<CommandMeta>> registeredCommands = new ConcurrentHashMap<>();
    @Getter private final Map<String, ServiceMeta> registeredServices = new ConcurrentHashMap<>();

    private final Plugin plugin;

    protected CommandsBungee(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.registerType(new CommandsBungeeTypes());
        this.errorHandler(new BungeeErrorHandler(this));
        this.resultHandler(new BungeeResultHandler());
        this.accessHandler(new BungeeAccessHandler(this));
        this.completionHandler(new BungeeCompletionHandler());
    }

    public static CommandsBungee of(@NonNull Plugin plugin) {
        CommandsBungee commandsBungee = new CommandsBungee(plugin);
        commandsBungee.registerListeners();
        return commandsBungee;
    }

    @Override
    public void close() throws IOException {
        ProxyServer.getInstance().getPluginManager().getCommands().stream()
            .filter(entry -> this.registeredServices.containsKey(entry.getKey()))
            .forEach(entry -> ProxyServer.getInstance().getPluginManager().unregisterCommand(entry.getValue()));
    }

    public void registerListeners() {
    }

    @Override
    public Object resolveMissingArgument(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (ProxiedPlayer.class.equals(paramType) && (param.getAnnotation(Context.class) != null)) {
            return data.get("sender", ProxiedPlayer.class);
        }

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

        Command bungeeCommand = new Wrapper(serviceLabel, this, service, metas);
        ProxyServer.getInstance().getPluginManager().registerCommand(this.plugin, bungeeCommand);
    }

    /*
      @deprecated Unsafe non-stable API allowing to access okaeri-commands from Bungee API
     */
    @Getter
    @Setter
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class Wrapper extends Command implements TabExecutor {

        private CommandsBungee commands;
        private ServiceMeta service;
        private List<CommandMeta> metas;

        protected Wrapper(@NonNull String name, @NonNull CommandsBungee commands, @NonNull ServiceMeta service, @NonNull List<CommandMeta> metas) {
            super(name, null /* FIXME: read class annotation permission */, service.getAliases().toArray(new String[0]));
            this.commands = commands;
            this.service = service;
            this.metas = metas;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {

            CommandData data = new CommandData();
            data.add("sender", sender);

            try {
                this.commands.executeCommand(this.service, data, sender, this.getName(), args);
            } catch (CommandException exception) {
                this.commands.handleError(data, Invocation.of(this.service, this.getName(), args), exception);
            }
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

            CommandData data = new CommandData();
            data.add("sender", sender);

            return this.commands.complete(this.metas,
                Invocation.of(this.service, this.getName(), args),
                data
            );
        }
    }

    private void executeCommand(@NonNull ServiceMeta service, @NonNull CommandData data, @NonNull CommandSender sender, @NonNull String label, @NonNull String[] args) {

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
            this.plugin.getProxy().getScheduler().runAsync(this.plugin, prepareAndExecuteAsync);
            return;
        }

        this.handleExecution(sender, invocation, data);
        Duration durationWithInvoke = Duration.between(Instant.now(), start);

        if (durationWithInvoke.compareTo(TOTAL_SYNC_WARN_TIME) <= 0) {
            return;
        }

        this.syncTimeWarn(service, executor, fullCommand, data, durationWithInvoke, "total");
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

        throw new RuntimeException("Unknown return value for errorHandler [allowed: BungeeResponse, CharSequence, null]", throwable);
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

            throw new RuntimeException("Unknown return value for executor [allowed: BungeeResponse, CharSequence, null]");
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
