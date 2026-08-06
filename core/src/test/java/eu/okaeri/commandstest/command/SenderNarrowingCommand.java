package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commandstest.sender.TestConsole;
import eu.okaeri.commandstest.sender.TestPlayer;
import eu.okaeri.commandstest.sender.TestSender;

@Command(label = "narrow")
public class SenderNarrowingCommand implements CommandService {

    @Executor
    public String player(TestPlayer sender) {
        return "player:" + sender.getName();
    }

    @Executor
    public String console(TestConsole sender) {
        return "console";
    }

    @Executor
    public String any(TestSender sender) {
        return "any:" + sender.getName();
    }

    @Executor
    public String annotated(@Context(invalid = "players only!") TestPlayer sender) {
        return "annotated:" + sender.getName();
    }

    @Executor
    public String plain(@Arg String value) {
        return "plain:" + value;
    }
}
