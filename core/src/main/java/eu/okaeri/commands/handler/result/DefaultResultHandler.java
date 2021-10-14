package eu.okaeri.commands.handler.result;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public class DefaultResultHandler implements ResultHandler {

    @Override
    public boolean handle(Object result, @NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext) {
        return true;
    }
}
