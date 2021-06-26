package eu.okaeri.commands;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.registry.OkaeriCommandsRegistry;
import eu.okaeri.commands.type.OkaeriCommandsTypes;

public final class CommandsManager {

    public static Commands create(CommandsAdapter adapter) {
        OkaeriCommandsRegistry registry = new OkaeriCommandsRegistry(adapter);
        OkaeriCommandsTypes types = new OkaeriCommandsTypes();
        Commands commands = new OkaeriCommands(adapter, registry, types);
        adapter.setCore(commands);
        return commands;
    }
}
