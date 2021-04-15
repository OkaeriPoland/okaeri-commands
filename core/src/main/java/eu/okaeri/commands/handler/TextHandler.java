package eu.okaeri.commands.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public interface TextHandler {
    String resolve(CommandContext commandContext, InvocationContext invocationContext, String text);
}
