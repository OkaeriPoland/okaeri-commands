package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;

@Command(label = "example-sa")
public class ExampleStaticArgsCommand implements CommandService {

    @Executor(pattern = "dummy")
    public int _dummy1() {
        return 1;
    }

    @Executor(pattern = "dummy dummy2")
    public int _dummy2() {
        return 2;
    }
}
