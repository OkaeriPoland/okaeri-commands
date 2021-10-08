package eu.okaeri.commands.bukkit;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public final class CommandsBukkitUnsafe {

    @SneakyThrows
    public static CommandMap getCommandMap() {
        Server server = Bukkit.getServer();
        Field commandMapField = server.getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return  (CommandMap) commandMapField.get(server);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static Map<String, Command> getKnownCommands() {
        CommandMap commandMap = getCommandMap();
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        return  (Map<String, Command>) knownCommandsField.get(commandMap);
    }

    @SneakyThrows
    public static Optional<Command> getCommand(String label) {
        return Optional.ofNullable(getKnownCommands().get(label));
    }

    @SneakyThrows
    public static boolean unregister(String label) {
        Command command = getKnownCommands().remove(label);
        if (command == null) {
            return false;
        }
        command.unregister(getCommandMap());
        return true;
    }
}
