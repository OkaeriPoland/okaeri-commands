package eu.okaeri.commands.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public interface ResultHandler {
    boolean onResult(Object result, CommandContext commandContext, InvocationContext invocationContext);
}
