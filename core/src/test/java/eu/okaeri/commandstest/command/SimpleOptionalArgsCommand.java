package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Option;

@Command(label = "simple-oa")
public class SimpleOptionalArgsCommand implements CommandService {

    @Executor(pattern = {"static", "static2", "stat ic"})
    public boolean _static() {
        return true;
    }

    @Executor(pattern = "?")
    public Object _optional(@Arg Option<String> data) {
        return data;
    }
}
