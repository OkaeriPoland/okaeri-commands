package eu.okaeri.commands.handler.completion;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.util.List;

public interface CompletionHandler {
    List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);
}
