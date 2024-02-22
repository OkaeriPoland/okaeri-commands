package eu.okaeri.commands.handler.error;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;

public class DefaultErrorHandler implements ErrorHandler {

    @Override
    public Object handle(CommandData data, Invocation invocation, Throwable exception) {
        throw new RuntimeException("Unhandled exception", exception);
    }
}
