package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SimpleNamedCompletionHandler implements NamedCompletionHandler {

    private static final int DEFAULT_FILTER_LIMIT = 10;
    private final Supplier<Stream<String>> provider;

    @Override
    public List<String> complete(@NonNull CompletionMeta completionData, @NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        String lastArg = invocationContext.isOpenArgs() ? "" : invocationContext.getLastArg().toLowerCase(Locale.ROOT);
        Map<String, String> data = completionData.getData(argument.getName());
        int filterLimit = data.containsKey("limit") ? Integer.parseInt(data.get("limit")) : DEFAULT_FILTER_LIMIT;

        return this.provider.get()
                .filter(name -> lastArg.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(lastArg))
                .limit(filterLimit)
                .collect(Collectors.toList());
    }
}
