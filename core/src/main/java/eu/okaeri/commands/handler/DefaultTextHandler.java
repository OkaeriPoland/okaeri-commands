package eu.okaeri.commands.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public class DefaultTextHandler implements TextHandler {

    @Override
    public String resolve(CommandContext commandContext, InvocationContext invocationContext, String text) {
        return text;
    }
}
