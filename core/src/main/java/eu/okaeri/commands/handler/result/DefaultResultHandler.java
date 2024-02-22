package eu.okaeri.commands.handler.result;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public class DefaultResultHandler implements ResultHandler {

    @Override
    public boolean handle(Object result, @NonNull CommandData data, @NonNull Invocation invocation) {
        return true;
    }
}
