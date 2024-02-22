package eu.okaeri.commands.handler.text;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public class DefaultTextHandler implements TextHandler {

    @Override
    public String resolve(@NonNull String text) {
        return text;
    }

    @Override
    public String resolve(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull String text) {
        return text;
    }
}
