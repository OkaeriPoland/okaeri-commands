package eu.okaeri.commands.handler.argument;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.lang.reflect.Parameter;

public class DefaultMissingArgumentHandler implements MissingArgumentHandler {

    @Override
    public Object resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull CommandMeta command, @NonNull Parameter param, int index) {
        Class<?> paramType = param.getType();
        if (CommandContext.class.isAssignableFrom(paramType)) {
            return commandContext;
        }
        return null;
    }
}
