package eu.okaeri.commands;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Parameter;

@Getter
@Setter(AccessLevel.PROTECTED)
public abstract class CommandsAdapter {

    private OkaeriCommands core;

    public String resolveText(CommandContext context, String text) {
        return text;
    }

    public Object resolveMissingArgument(CommandMeta command, Parameter param, int i) {
        return null;
    }

    @SneakyThrows
    public <T extends CommandService> T createInstance(Class<T> clazz) {
        return clazz.newInstance();
    }

    public void onRegister(CommandMeta command) {
    }
}
