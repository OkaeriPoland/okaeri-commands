package eu.okaeri.commands.meta;

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExecutorMeta {

    public static List<ExecutorMeta> of(Method method) {

        Executor executor = method.getAnnotation(Executor.class);
        if (executor == null) {
            throw new IllegalArgumentException("cannot create ExecutorMeta from Method without @Executor annotation");
        }

        List<String> patterns = (executor.pattern().length == 0)
                ? Collections.singletonList(method.getName())
                : Arrays.asList(executor.pattern());

        return patterns.stream().map(pattern -> {

            // create
            ExecutorMeta cmdExecutor = new ExecutorMeta();
            cmdExecutor.method = method;
            cmdExecutor.arguments = Collections.unmodifiableList(Arrays.stream(method.getParameters()).map(ArgumentMeta::of).collect(Collectors.toList()));
            cmdExecutor.fallback = executor.fallback();
            cmdExecutor.async = executor.async();
            cmdExecutor.pattern = PatternMeta.of(pattern);
            cmdExecutor.description = executor.description();
            cmdExecutor.usage = executor.usage();

            // validate if all arguments used in method definition ale present in the pattern
            for (ArgumentMeta argument : cmdExecutor.arguments) {

                String argumentName = argument.getName();
                String patternRaw = cmdExecutor.pattern.getRaw();

                if (argument.isRawArgs()) {
                    continue;
                }

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
        }).collect(Collectors.toList());
    }

    private Method method;
    private List<ArgumentMeta> arguments;

    private boolean fallback;
    private boolean async;
    private PatternMeta pattern;
    private String description;
    private String usage;
}
