package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CompletionMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.util.List;

public interface NamedCompletionHandler {
    List<String> complete(@NonNull CompletionMeta completionData, @NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);
}
