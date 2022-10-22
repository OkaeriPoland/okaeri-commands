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

public interface CompletionHandler {

    static final List<String> BOOLEAN_COMPLETIONS = Collections.unmodifiableList(Arrays.asList("true", "false"));
    static final int FALLBACK_LIMIT = 1000;

    List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    static int getLimit(@NonNull ArgumentMeta argumentMeta, @NonNull InvocationContext invocationContext) {
        return getData(argumentMeta, invocationContext, "limit", () -> FALLBACK_LIMIT, Integer::parseInt);
    }

    static <T> T getData(@NonNull ArgumentMeta argumentMeta, @NonNull InvocationContext invocationContext, @NonNull String name, @NonNull Supplier<T> fallback, @NonNull Function<String, T> resolver) {

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

    static <T> List<T> filter(int limit, @NonNull Predicate<T> filter, @NonNull Stream<T> stream) {
        try (Stream<T> finalStream = stream) {
            return stream
                .filter(filter)
                .limit(limit)
                .collect(Collectors.toList());
        }
    }

    static Predicate<String> stringFilter(@NonNull InvocationContext invocationContext) {
        String lastArg = invocationContext.isOpenArgs() ? "" : invocationContext.getLastArg().toLowerCase(Locale.ROOT);
        return name -> lastArg.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(lastArg);
    }
}
