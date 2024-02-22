package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Deprecated
@RequiredArgsConstructor
public class SimpleNamedCompletionHandler implements NamedCompletionHandler {

    private final Supplier<Stream<String>> provider;

    @Override
    public List<String> complete(@NonNull CompletionMeta completionData, @NonNull ArgumentMeta argument, @NonNull Invocation invocation, @NonNull CommandData data) {

        int limit = CompletionHandler.getLimit(argument, invocation);
        Predicate<String> stringFilter = CompletionHandler.stringFilter(invocation);

        return CompletionHandler.filter(limit, stringFilter, this.provider.get());
    }
}
