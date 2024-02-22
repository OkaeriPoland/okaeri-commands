package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DefaultCompletionHandler implements CompletionHandler {

    @Override
    public void registerNamed(@NonNull Commands commands) {
        commands.registerCompletion("default:enum", (completion, argument, invocation, data) ->
            this.completeEnum(invocation, argument.getType(), this.getLimit(argument, invocation)));
        commands.registerCompletion("default:boolean", (completion, argument, invocation, data) ->
            BOOLEAN_COMPLETIONS);
    }

    protected List<String> completeEnum(@NotNull Invocation invocation, Class<?> type, int limit) {
        return this.filter(limit, this.stringFilter(invocation), Arrays.stream(type.getEnumConstants())
            .map(Enum.class::cast)
            .map(Enum::name)
            .map(String::toLowerCase));
    }

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull Invocation invocation, @NonNull CommandData data) {

        Class<?> type = argument.getType();
        int limit = this.getLimit(argument, invocation);

        if (type.isEnum()) {
            return this.completeEnum(invocation, type, limit);
        }

        if (boolean.class.isAssignableFrom(type)) {
            return BOOLEAN_COMPLETIONS;
        }

        return Collections.emptyList();
    }

    protected int getLimit(@NonNull ArgumentMeta argumentMeta, @NonNull Invocation invocation) {
        return CompletionHandler.getLimit(argumentMeta, invocation);
    }

    protected <T> T getData(@NonNull ArgumentMeta argumentMeta, @NonNull Invocation invocation, @NonNull String name, @NonNull Supplier<T> fallback, @NonNull Function<String, T> resolver) {
        return CompletionHandler.getData(argumentMeta, invocation, name, fallback, resolver);
    }

    @Deprecated
    protected <T> List<T> filter(@NonNull Predicate<T> filter, int limit, @NonNull Stream<T> stream) {
        return CompletionHandler.filter(limit, filter, stream);
    }

    protected <T> List<T> filter(int limit, @NonNull Predicate<T> filter, @NonNull Stream<T> stream) {
        return CompletionHandler.filter(limit, filter, stream);
    }

    protected Predicate<String> stringFilter(@NonNull Invocation invocation) {
        return CompletionHandler.stringFilter(invocation);
    }
}
