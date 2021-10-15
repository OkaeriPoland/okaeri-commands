package eu.okaeri.commands.handler.text;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public class DefaultTextHandler implements TextHandler {

    @Override
    public String resolve(@NonNull String text) {
        return text;
    }

    @Override
    public String resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text) {
        return text;
    }
}
