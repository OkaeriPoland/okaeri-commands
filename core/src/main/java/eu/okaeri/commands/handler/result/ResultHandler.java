package eu.okaeri.commands.handler.result;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface ResultHandler {
    boolean handle(Object result, @NonNull CommandData data, @NonNull Invocation invocation);
}
