package eu.okaeri.commands;

public final class CommandsManager {

    public static OkaeriCommands create(CommandsAdapter adapter) {
        OkaeriCommands commands = new OkaeriCommands(adapter);
        adapter.setCore(commands);
        return commands;
    }
}
