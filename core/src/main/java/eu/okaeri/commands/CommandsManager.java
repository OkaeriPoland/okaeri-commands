package eu.okaeri.commands;

import eu.okaeri.commands.adapter.CommandsAdapter;

public final class CommandsManager {

    public static OkaeriCommands create(CommandsAdapter adapter) {
        OkaeriCommands commands = new OkaeriCommands(adapter);
        adapter.setCore(commands);
        return commands;
    }
}
