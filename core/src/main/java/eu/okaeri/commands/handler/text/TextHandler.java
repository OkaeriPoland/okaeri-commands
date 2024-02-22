package eu.okaeri.commands.handler.text;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface TextHandler {

    String resolve(@NonNull String text);

    String resolve(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull String text);
}
