package eu.okaeri.commandstest;

import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.cli.CommandsCli;

import java.lang.reflect.InvocationTargetException;

public final class TestCommands {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {

        OkaeriCommands commands = CommandsManager.create(new CommandsCli());
        commands.register(new ExampleCommand());

        commands.call("cmd hello siema");
        commands.call("cmd bk");
        commands.call("cmd bk xdd");
        commands.call("cmd bk xddd ddd");
        commands.call("cmd player Daffit set essentials.spawn true world_nether");
    }
}

