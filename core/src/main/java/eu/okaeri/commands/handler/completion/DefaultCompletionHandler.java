package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultCompletionHandler implements CompletionHandler {

    private static final List<String> BOOLEAN_COMPLETIONS = Arrays.asList("true", "false");
    private static final int FALLBACK_LIMIT = 1000;

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = argument.getType();
        Predicate<String> stringFilter = this.stringFilter(invocationContext);
        int limit = this.getLimit(argument, invocationContext);

        if (type.isEnum()) {
            return this.filter(stringFilter, limit, Arrays.stream(type.getEnumConstants())
                .map(Enum.class::cast)
                .map(Enum::name)
                .map(String::toLowerCase));
        }

        if (boolean.class.isAssignableFrom(type)) {
            return BOOLEAN_COMPLETIONS;
        }

        return Collections.emptyList();
    }

    protected int getLimit(@NonNull ArgumentMeta argumentMeta, @NonNull InvocationContext invocationContext) {
        return this.getData(argumentMeta, invocationContext, "limit", () -> FALLBACK_LIMIT, Integer::parseInt);
    }

    protected <T> T getData(@NonNull ArgumentMeta argumentMeta, @NonNull InvocationContext invocationContext, @NonNull String name, @NonNull Supplier<T> fallback, @NonNull Function<String, T> resolver) {

        if (invocationContext.getCommand() == null) {
            return fallback.get();
        }

        CompletionMeta completion = invocationContext.getCommand().getExecutor().getCompletion();
        Map<String, String> data = completion.getData(argumentMeta.getName());

        if (data.containsKey(name)) {
            return resolver.apply(data.get(name));
        }

        return fallback.get();
    }

    protected <T> List<T> filter(Predicate<T> filter, int limit, Stream<T> stream) {
        return stream
            .filter(filter)
            .limit(limit)
            .collect(Collectors.toList());
    }

    protected Predicate<String> stringFilter(InvocationContext invocationContext) {
        String lastArg = invocationContext.isOpenArgs() ? "" : invocationContext.getLastArg().toLowerCase(Locale.ROOT);
        return name -> lastArg.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(lastArg);
    }
}
