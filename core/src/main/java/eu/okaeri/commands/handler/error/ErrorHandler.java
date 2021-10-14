package eu.okaeri.commands.handler.error;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public interface ErrorHandler {
    Object handle(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable exception);
}
