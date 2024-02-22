package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.util.List;

public interface NamedCompletionHandler {
    List<String> complete(@NonNull CompletionMeta completionData, @NonNull ArgumentMeta argument, @NonNull Invocation invocation, @NonNull CommandData data);
}
