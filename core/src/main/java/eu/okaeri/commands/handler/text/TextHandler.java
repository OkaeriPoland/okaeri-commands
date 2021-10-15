package eu.okaeri.commands.handler.text;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public interface TextHandler {

    String resolve(@NonNull String text);

    String resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text);
}
