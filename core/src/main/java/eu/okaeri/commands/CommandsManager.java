package eu.okaeri.commands;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.registry.OkaeriCommandsRegistry;

public final class CommandsManager {

    public static Commands create(CommandsAdapter adapter) {
        OkaeriCommandsRegistry registry = new OkaeriCommandsRegistry(adapter);
        Commands commands = new OkaeriCommands(adapter, registry);
        adapter.setCore(commands);
        return commands;
    }
}
