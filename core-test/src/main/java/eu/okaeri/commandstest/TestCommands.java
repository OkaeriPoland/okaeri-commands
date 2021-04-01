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
    }
}

