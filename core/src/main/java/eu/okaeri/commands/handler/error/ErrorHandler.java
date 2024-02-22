package eu.okaeri.commands.handler.error;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface ErrorHandler {
    Object handle(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable exception);
}
