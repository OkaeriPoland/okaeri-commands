package eu.okaeri.commands.adapter;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Parameter;

@Getter
@Setter
public abstract class CommandsAdapter {

    private Commands core;

    public String resolveText(CommandContext commandContext, InvocationContext invocationContext, String text) {
        return text;
    }

    public Object resolveMissingArgument(CommandContext commandContext, InvocationContext invocationContext, CommandMeta command, Parameter param, int i) {

        Class<?> paramType = param.getType();
        if (CommandContext.class.isAssignableFrom(paramType)) {
            return commandContext;
        }

        return null;
    }

    @SneakyThrows
    public <T extends CommandService> T createInstance(Class<T> clazz) {
        return clazz.newInstance();
    }

    public void onRegister(CommandMeta command) {
    }
}
