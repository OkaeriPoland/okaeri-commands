package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.service.CommandService;

@Command(label = "tricky", aliases = "triccy")
public class SimpleTrickyCommand implements CommandService {

    @Executor(pattern = {"list", "lists"})
    public String lists() {
        return "lists";
    }

    @Executor
    public String _list(@Arg("list") String list) {
        return list;
    }
}
