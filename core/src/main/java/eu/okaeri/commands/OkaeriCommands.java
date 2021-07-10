package eu.okaeri.commands;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.annotation.Label;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.registry.CommandsRegistry;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.CommandsTypes;
import eu.okaeri.commands.type.CommandsTypesPack;
import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OkaeriCommands implements Commands {

    private final CommandsAdapter adapter;
    private final CommandsRegistry registry;
    private final CommandsTypes types;

    @Override
    public Commands register(@NonNull Class<? extends CommandService> clazz) {
        this.registry.register(clazz);
        return this;
    }

    @Override
    public Commands register(@NonNull CommandService service) {
        this.registry.register(service);
        return this;
    }

    @Override
    public Commands register(@NonNull TypeResolver typeResolver) {
        this.types.register(typeResolver);
        return this;
    }

    @Override
    public Commands register(@NonNull CommandsTypesPack typesPack) {
        this.types.register(typesPack);
        return this;
    }

    @Override
    @Deprecated
    public Object call(@NonNull String command) throws InvocationTargetException, IllegalAccessException {
        Optional<InvocationContext> context = this.invocationMatch(command);
        if (!context.isPresent()) {
            throw new IllegalArgumentException("cannot call '" + command + "', no executor available");
        }
        InvocationContext invocationContext = context.get();
        return this.invocationPrepare(invocationContext, new CommandContext()).call();
    }

    @Override
    public Optional<InvocationContext> invocationMatch(@NonNull String command) {

        String[] parts = command.split(" ", 2);
        String label = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";

        Optional<CommandMeta> commandMetas = this.getRegistry().findByLabelAndArgs(label, args);
        if (!commandMetas.isPresent()) {
            return Optional.empty();
        }

        CommandMeta commandMeta = commandMetas.get();
        return Optional.of(InvocationContext.of(commandMeta, label, args));
    }

    @Override
    public InvocationMeta invocationPrepare(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        String args = invocationContext.getArgs();
        CommandMeta commandMeta = invocationContext.getCommand();
        ExecutorMeta executor = commandMeta.getExecutor();
        PatternMeta pattern = executor.getPattern();
        List<ArgumentMeta> arguments = executor.getArguments();

        Map<Integer, Object> callArguments = new LinkedHashMap<>();
        String[] argsArr = args.split(" ");

        for (ArgumentMeta argument : arguments) {

            String value = pattern.getValueByArgument(argument, argsArr);
            Optional<TypeResolver> typeResolverOptional = this.getTypes().findByType(argument.getParameterizedType());

            if (!typeResolverOptional.isPresent()) {
                throw new IllegalArgumentException("method argument of type " + argument.getType() + " cannot be resolved");
            }

            Object resolvedValue;
            try {
                if (!argument.isOptional() && (value == null)) {
                    throw new IllegalArgumentException("non-optional argument was null");
                } else if (argument.isOptional() && (value == null)) {
                    resolvedValue = null;
                } else {
                    resolvedValue = typeResolverOptional.get().resolve(invocationContext, commandContext, argument, value);
                }
            } catch (Exception exception) {
                throw new CommandException(argument.getName() + " - " + exception.getMessage());
            }

            callArguments.put(argument.getIndex(), argument.wrap(resolvedValue));
        }

        if (arguments.size() != callArguments.size()) {
            throw new IllegalArgumentException("method arguments size (" + arguments.size() + ") does not match call arguments size (" + callArguments.size() + ")");
        }

        Method executorMethod = executor.getMethod();
        Parameter[] methodParameters = executorMethod.getParameters();
        int parametersLength = methodParameters.length;
        Object[] call = new Object[parametersLength];

        for (int i = 0; i < parametersLength; i++) {

            // argument present
            Object callArgument = callArguments.get(i);
            if (callArgument != null) {
                call[i] = callArgument;
                continue;
            }

            // check for RawArgs
            Parameter param = methodParameters[i];
            Class<?> paramType = param.getType();
            if (param.getAnnotation(RawArgs.class) != null) {
                if (CharSequence.class.isAssignableFrom(paramType)) {
                    call[i] = args;
                    continue;
                }
                if (List.class.isAssignableFrom(paramType)) {
                    call[i] = Arrays.asList(argsArr);
                    continue;
                }
                if (paramType.isArray() && CharSequence.class.isAssignableFrom(paramType.getComponentType())) {
                    call[i] = argsArr;
                    continue;
                }
                throw new IllegalArgumentException("@RawArgs type cannot be " + paramType + " [allowed: String, List<String>, String[]]");
            }

            // check for label
            if (param.getAnnotation(Label.class) != null) {
                if (CharSequence.class.isAssignableFrom(paramType)) {
                    call[i] = invocationContext.getLabel();
                    continue;
                }
                throw new IllegalArgumentException("@Label type cannot be " + paramType + " [allowed: String]");
            }

            // pass to adapter for missing elements
            call[i] = this.getAdapter().resolveMissingArgument(commandContext, invocationContext, commandMeta, param, i);
        }

        return InvocationMeta.of(executorMethod, call, commandMeta.getService(), executor);
    }
}
