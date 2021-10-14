package eu.okaeri.commands.handler.error;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public class DefaultErrorHandler implements ErrorHandler {

    @Override
    public Object handle(CommandContext commandContext, InvocationContext invocationContext, Throwable exception) {
        throw new RuntimeException("Unhandled exception", exception);
    }
}
