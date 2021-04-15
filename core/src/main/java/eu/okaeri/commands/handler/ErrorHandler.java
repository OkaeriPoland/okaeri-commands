package eu.okaeri.commands.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public interface ErrorHandler {
    Object onError(CommandContext commandContext, InvocationContext invocationContext, Throwable exception);
}
