package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.cli.CommandsCli;

import java.lang.reflect.InvocationTargetException;

public final class TestCommands {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {

        Commands commands = CommandsManager.create(new CommandsCli());
        commands.register(ExampleCommand.class); // DI
        // commands.register(new ExampleCommand());

        test(commands);
    }

    public static void test(Commands commands) throws InvocationTargetException, IllegalAccessException {
        commands.call("cmd hello siema");
        commands.call("cmd bk");
        commands.call("cmd bk xdd");
        commands.call("cmd bk xddd ddd");
        commands.call("cmd player Daffit set essentials.spawn true world_nether");
        commands.call("cmd player Daffit set essentials.spawn true");
        commands.call("cmd player Daffit sett essentials.spawn true");
        commands.call("cmd bk xdxd");
        commands.call("cmd bk xddd ddxd");
        commands.call("cmd player Daffit set essentials.spawn true world");
    }
}
