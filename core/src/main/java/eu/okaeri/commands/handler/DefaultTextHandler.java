package eu.okaeri.commands.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public class DefaultTextHandler implements TextHandler {

    @Override
    public String resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text) {
        return text;
    }
}
