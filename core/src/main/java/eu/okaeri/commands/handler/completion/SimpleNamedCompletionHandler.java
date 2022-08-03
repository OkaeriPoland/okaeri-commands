package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SimpleNamedCompletionHandler implements NamedCompletionHandler {

    private final Supplier<Stream<String>> provider;

    @Override
    public List<String> complete(@NonNull CompletionMeta completionData, @NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        int limit = CompletionHandler.getLimit(argument, invocationContext);
        Predicate<String> stringFilter = CompletionHandler.stringFilter(invocationContext);

        return CompletionHandler.filter(limit, stringFilter, this.provider.get());
    }
}
