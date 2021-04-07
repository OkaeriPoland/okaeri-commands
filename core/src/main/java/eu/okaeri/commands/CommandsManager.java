package eu.okaeri.commands;

import eu.okaeri.commands.adapter.CommandsAdapter;

public final class CommandsManager {

    public static Commands create(CommandsAdapter adapter) {
        Commands commands = new OkaeriCommands(adapter);
        adapter.setCore(commands);
        return commands;
    }
}
