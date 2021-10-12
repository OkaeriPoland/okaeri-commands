package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;

@Command(label = "simple-ra")
public class SimpleRequiredArgsCommand implements CommandService {

    @Executor(pattern = {"static", "static2", "stat ic"})
    public boolean _static() {
        return true;
    }

    @Executor(pattern = "*")
    public String _required(@Arg String data) {
        return data;
    }

    @Executor(pattern = "* *")
    public String _required_required(@Arg String data1, @Arg String data2) {
        return data1 + " " + data2;
    }
}
