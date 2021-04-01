package eu.okaeri.commands;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
public abstract class CommandsAdapter {

    private OkaeriCommands core;

    public String resolveText(CommandContext context, String text) {
        return text;
    }

    public void onRegister(CommandMeta command) {
    }
}
