package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.exception.ExceptionSource;
import eu.okaeri.commands.service.CommandContext;

public interface ErrorHandler {
    Object onError(CommandContext context, Throwable exception, ExceptionSource source);
}
