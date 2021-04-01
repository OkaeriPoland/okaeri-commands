package eu.okaeri.commands;

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.service.CommandService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OkaeriCommands {

    private List<CommandMeta> registeredCommands = new ArrayList<>();
    private final CommandsAdapter adapter;

    public void register(Class<? extends CommandService> clazz) {
        this.register(this.adapter.createInstance(clazz));
    }

    public void register(CommandService service) {

        Class<? extends CommandService> clazz = service.getClass();
        for (Method method : clazz.getDeclaredMethods()) {

            Executor executor = method.getAnnotation(Executor.class);
            if (executor == null) {
                continue;
            }

            List<CommandMeta> commands = CommandMeta.of(service, method);
            for (CommandMeta command : commands) {
                this.registeredCommands.add(command);
                this.adapter.onRegister(command);
            }
        }
    }

    public List<CommandMeta> findByLabel(String label) {
        return this.registeredCommands.stream()
                .filter(candidate -> candidate.isLabelApplicable(label))
                .collect(Collectors.toList());
    }

    public List<CommandMeta> findByLabelAndArgs(String label, String args) {
        return this.findByLabel(label).stream()
                .sorted(Comparator.comparing(meta -> meta.getExecutor().getPattern().getRaw(), Comparator.reverseOrder()))
                .filter(meta -> meta.getExecutor().getPattern().matches(args))
                .collect(Collectors.toList());
    }

    public Object call(String command) throws InvocationTargetException, IllegalAccessException {

        String[] parts = command.split(" ", 2);
        String label = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";

        List<CommandMeta> commandMetas = this.findByLabelAndArgs(label, args);
        if (commandMetas.isEmpty()) {
            throw new IllegalArgumentException("cannot call '" + command + "', no executor available");
        }

        commandMetas.forEach(meta -> System.out.println(meta.getExecutor().getMethod() + " " + meta.getExecutor().getPattern()));
        CommandMeta commandMeta = commandMetas.get(0);
        ExecutorMeta executor = commandMeta.getExecutor();
        PatternMeta pattern = executor.getPattern();
        List<ArgumentMeta> arguments = executor.getArguments();

        Map<Integer, Object> callArguments = new LinkedHashMap<>();
        String[] argsArr = args.split(" ");

        for (ArgumentMeta argument : arguments) {
            String value = pattern.getValueByArgument(argument, args);
            callArguments.put(argument.getIndex(), argument.wrap(value));
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
            if (callArguments.containsKey(i)) {
                call[i] = callArguments.get(i);
                continue;
            }

            // check for RawArgs
            Parameter param = methodParameters[i];
            if (param.getAnnotation(RawArgs.class) != null) {
                Class<?> paramType = param.getType();
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
                throw new IllegalAccessException("@RawArgs type cannot be " + paramType + " [allowed: String, List<String>, String[]]");
            }

            // pass to adapter for missing elements
            call[i] = this.adapter.resolveMissingArgument(commandMeta, param, i);
        }

        return executorMethod.invoke(commandMeta.getService().getImplementor(), call);
    }
}
