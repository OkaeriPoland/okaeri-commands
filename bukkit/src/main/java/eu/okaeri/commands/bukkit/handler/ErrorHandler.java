package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.exception.ExceptionSource;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public interface ErrorHandler {
    Object onError(CommandContext commandContext, InvocationContext invocationContext, Throwable exception, ExceptionSource source);
}
