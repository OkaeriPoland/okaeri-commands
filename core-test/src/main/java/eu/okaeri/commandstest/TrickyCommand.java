package eu.okaeri.commandstest;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.service.CommandService;

@ServiceDescriptor(label = "tricky", aliases = "triccy")
public class TrickyCommand implements CommandService {

    @Executor(pattern = {"list", "lists"})
    public String lists() {
        return "lists";
    }

    @Executor(pattern = "*")
    public String vote(@Arg("list") String list) {
        return "list: " + list;
    }
}
