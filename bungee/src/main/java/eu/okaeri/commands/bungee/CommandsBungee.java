package eu.okaeri.commands.bungee;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.bungee.handler.*;
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
    public static final Duration INVOKE_SYNC_WARN_TIME = Duration.ofMillis(10);

    private final Map<String, List<CommandMeta>> registeredCommands = new ConcurrentHashMap<>();
    private final @Getter Map<String, ServiceMeta> registeredServices = new ConcurrentHashMap<>();

    private final Plugin plugin;

    protected CommandsBungee(@NonNull Plugin plugin) {
        this.plugin = plugin;
        this.registerType(new CommandsBungeeTypes());
        this.errorHandler(new BungeeErrorHandler(this));
        this.resultHandler(new BungeeResultHandler());
        this.accessHandler(new BungeeAccessHandler(this));
        this.completionHandler(new BungeeCompletionHandler());
        this.schedulingHandler(new BungeeSchedulingHandler(plugin));
    }

    public static CommandsBungee of(@NonNull Plugin plugin) {
        CommandsBungee commandsBungee = new CommandsBungee(plugin);
        commandsBungee.registerListeners();
        return commandsBungee;
    }

    @Override
    public void close() {
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
        Duration preInvokeTime = Duration.between(start, Instant.now());

        if (preInvokeTime.compareTo(PRE_INVOKE_SYNC_WARN_TIME) > 0) { // TODO: replace with TimingHandler?
            this.plugin.getLogger().warning(BungeeSchedulingHandler.syncTimeWarn(data, invocation, preInvokeTime, "pre-invoke"));
        }

        Runnable prepareAndExecute = () -> this.handleExecution(sender, invocation, data);
        this.getSchedulingHandler().run(data, invocation, prepareAndExecute);
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
}
