package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.service.Option;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultCompletionHandler implements CompletionHandler {

    private static final List<String> BOOLEAN_COMPLETIONS = Arrays.asList("true", "false");
    private static final int DEFAULT_FILTER_LIMIT = 10;

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = this.resolveType(argument);
        Predicate<String> stringFilter = this.stringFilter(invocationContext);

        if (type.isEnum()) {
            return this.filter(stringFilter, Arrays.stream(type.getEnumConstants())
                    .map(Enum.class::cast)
                    .map(Enum::name)
                    .map(String::toLowerCase));
        }

        if (boolean.class.isAssignableFrom(type)) {
            return BOOLEAN_COMPLETIONS;
        }

        return Collections.emptyList();
    }

    protected Class<?> resolveType(ArgumentMeta argument) {

        Class<?> type = argument.getType();
        Type parameterizedType = argument.getParameterizedType();

        if (Option.class.isAssignableFrom(type) || Optional.class.isAssignableFrom(type)) {
            Type rawType = ((ParameterizedType) parameterizedType).getRawType();
            if (rawType instanceof Class<?>) {
                type = (Class<?>) rawType;
            }
        }

        return type;
    }

    protected <T> List<T> filter(Predicate<T> filter, Stream<T> stream) {
        return stream
                .filter(filter)
                .limit(DEFAULT_FILTER_LIMIT)
                .collect(Collectors.toList());
    }

    protected Predicate<String> stringFilter(InvocationContext invocationContext) {
        String lastArg = invocationContext.isOpenArgs() ? "" : invocationContext.getLastArg().toLowerCase(Locale.ROOT);
        return name -> lastArg.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(lastArg);
    }
}
