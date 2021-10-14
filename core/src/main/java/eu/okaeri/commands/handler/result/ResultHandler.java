package eu.okaeri.commands.handler.result;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public interface ResultHandler {
    boolean handle(Object result, @NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext);
}
