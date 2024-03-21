package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Data
public class ExecutorMeta {

    private Method method;
    private List<ArgumentMeta> arguments;
    private PatternMeta pattern;
    private CompletionMeta completion;
    private String description;
    private String usage;
    private int index;

    public static List<ExecutorMeta> of(@NonNull Commands commands, @NonNull ServiceMeta serviceMeta, @NonNull Method method) {

        Executor executor = method.getAnnotation(Executor.class);
        if (executor == null) {
            throw new IllegalArgumentException("cannot create ExecutorMeta from Method without @Executor annotation");
        }

        AtomicInteger indexCounter = new AtomicInteger();
        String patternPrefix = serviceMeta.getPatternPrefix();

        boolean emptyPattern = executor.pattern().length == 0;
        List<String> patterns = emptyPattern
            ? Collections.singletonList(method.getName().startsWith("_") ? "" : method.getName())
            : Arrays.asList(executor.pattern());

        return patterns.stream()
            .map(commands::resolveText)
            .flatMap(pattern -> Arrays.stream(pattern.split(OkaeriCommands.SEPARATOR)))
            .map(pattern -> {

                // create
                ExecutorMeta cmdExecutor = new ExecutorMeta();
                cmdExecutor.method = method;

                List<ArgumentMeta> arguments = new ArrayList<>();
                for (int i = 0; i < method.getParameters().length; i++) {
                    Parameter parameter = method.getParameters()[i];
                    if (!ArgumentMeta.isArg(parameter)) {
                        continue;
                    }
                    arguments.add(ArgumentMeta.of(commands, parameter, i));
                }
                cmdExecutor.arguments = Collections.unmodifiableList(arguments);

                cmdExecutor.pattern = PatternMeta.of(commands, patternPrefix, pattern, cmdExecutor.arguments, emptyPattern);
                cmdExecutor.completion = CompletionMeta.of(commands, method);
                cmdExecutor.description = commands.resolveText(executor.description());
                cmdExecutor.usage = commands.resolveText(executor.usage());
                cmdExecutor.index = indexCounter.getAndIncrement();

                // validate if all arguments used in method definition ale present in the pattern
                for (ArgumentMeta argument : cmdExecutor.arguments) {

                    String argumentName = argument.getName();
                    String patternRaw = cmdExecutor.pattern.getRaw();

                    if (!cmdExecutor.pattern.getElementByName(argumentName).isPresent()) {
                        throw new IllegalArgumentException("method argument '" + argumentName + "' not found in the pattern '" + patternRaw + "' from [method: " + method + "]");
                    }
                }

                // validate if all pattern arguments are used in the method
                for (PatternElement element : cmdExecutor.pattern.getElements()) {

                    if (element instanceof StaticElement) {
                        continue;
                    }

                    String elementName = element.getName();
                    String patternRaw = cmdExecutor.pattern.getRaw();
                    boolean present = cmdExecutor.arguments.stream().anyMatch(argumentMeta -> elementName.equals(argumentMeta.getName()));

                    if (!present) {
                        throw new IllegalArgumentException("argument '" + elementName + "' from pattern '" + patternRaw + "' not found in [method: " + method + "]");
                    }
                }

                return cmdExecutor;
            })
            .collect(Collectors.toList());
    }
}
