package eu.okaeri.commands.velocity;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.velocity.handler.VelocityAccessHandler;
import eu.okaeri.commands.velocity.handler.VelocityCompletionHandler;
import eu.okaeri.commands.velocity.handler.VelocityErrorHandler;
import eu.okaeri.commands.velocity.handler.VelocityResultHandler;
import eu.okaeri.commands.velocity.type.CommandsVelocityTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CommandsVelocity extends OkaeriCommands {

    private static final Logger LOGGER = Logger.getLogger(CommandsVelocity.class.getSimpleName());

    private final Map<String, List<CommandMeta>> registeredCommands = new ConcurrentHashMap<>();
    private final @Getter Map<String, ServiceMeta> registeredServices = new ConcurrentHashMap<>();

    private final ProxyServer proxy;
    private final PluginContainer plugin;

    protected CommandsVelocity(@NonNull ProxyServer proxy, @NonNull PluginContainer plugin) {
        this.proxy = proxy;
        this.plugin = plugin;
        this.registerType(new CommandsVelocityTypes(proxy));
        this.errorHandler(new VelocityErrorHandler(proxy, this));
        this.resultHandler(new VelocityResultHandler());
        this.accessHandler(new VelocityAccessHandler(this));
        this.completionHandler(new VelocityCompletionHandler(proxy));
    }

    public static CommandsVelocity of(@NonNull ProxyServer proxy, @NonNull PluginContainer plugin) {
        CommandsVelocity commandsVelocity = new CommandsVelocity(proxy, plugin);
        commandsVelocity.registerListeners();
        return commandsVelocity;
    }

    @Override
    public void close() {
        this.getRegisteredServices().values().stream()
            .flatMap(service -> Stream.concat(Stream.of(service.getLabel()), service.getAliases().stream()))
            .forEach(this.proxy.getCommandManager()::unregister);
    }

    public void registerListeners() {
    }

    @Override
    public Object resolveMissingArgument(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (Player.class.equals(paramType) && (param.getAnnotation(Context.class) != null)) {
            return data.get("sender", Player.class);
        }

        if (CommandSource.class.equals(paramType) && data.has("sender", CommandSource.class)) {
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

        com.velocitypowered.api.command.CommandMeta velocityMeta = this.proxy.getCommandManager()
            .metaBuilder(serviceLabel)
            .aliases(service.getAliases().toArray(new String[0]))
            .plugin(this)
            .build();

        Command velocityCommand = new Wrapper(this, service, metas);
        this.proxy.getCommandManager().register(velocityMeta, velocityCommand);
    }

    /*
      @deprecated Unsafe non-stable API allowing to access okaeri-commands from Velocity API
     */
    @Getter
    @Setter
    @Deprecated
    @AllArgsConstructor
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class Wrapper implements SimpleCommand {

        private CommandsVelocity commands;
        private ServiceMeta service;
        private List<CommandMeta> metas;

        @Override
        public void execute(com.velocitypowered.api.command.SimpleCommand.Invocation invocation) {

            String name = invocation.alias();
            String[] args = invocation.arguments();
            CommandSource sender = invocation.source();

            CommandData data = new CommandData();
            data.add("sender", sender);

            try {
                this.commands.executeCommand(this.service, data, sender, name, args);
            } catch (CommandException exception) {
                this.commands.handleError(data, eu.okaeri.commands.service.Invocation.of(this.service, name, args), exception);
            }
        }

        @Override
        public CompletableFuture<List<String>> suggestAsync(com.velocitypowered.api.command.SimpleCommand.Invocation invocation) {

            CommandData data = new CommandData();
            data.add("sender", invocation.source());

            return CompletableFuture.supplyAsync(() -> this.commands.complete(this.metas,
                eu.okaeri.commands.service.Invocation.of(this.service, invocation.alias(), invocation.arguments()),
                data
            ));
        }

        @Override
        public boolean hasPermission(com.velocitypowered.api.command.SimpleCommand.Invocation invocation) {

            CommandData data = new CommandData();
            data.add("sender", invocation.source());

            return this.commands.getAccessHandler().allowAccess(this.service,
                eu.okaeri.commands.service.Invocation.of(this.service, invocation.alias(), invocation.arguments()),
                data
            );
        }
    }

    private void executeCommand(@NonNull ServiceMeta service, @NonNull CommandData data, @NonNull CommandSource sender, @NonNull String label, @NonNull String[] args) {

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
        this.handleExecution(sender, invocation, data);
    }

    private void handleError(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable throwable) {

        Object result = this.getErrorHandler().handle(data, invocation, throwable);
        if (result == null) {
            return;
        }

        CommandSource sender = data.get("sender", CommandSource.class);
        if (sender == null) {
            throw new RuntimeException("Cannot dispatch error", throwable);
        }

        if (this.getResultHandler().handle(result, data, invocation)) {
            return;
        }

        throw new RuntimeException("Unknown return value for errorHandler [allowed: Component, VelocityResponse, CharSequence, null]", throwable);
    }

    private void handleExecution(@NonNull CommandSource sender, @NonNull Invocation invocation, @NonNull CommandData data) {
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

            throw new RuntimeException("Unknown return value for executor [allowed: Component, VelocityResponse, CharSequence, null]");
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
