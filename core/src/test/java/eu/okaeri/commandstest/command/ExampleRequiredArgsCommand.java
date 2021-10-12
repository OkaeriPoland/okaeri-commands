package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.service.CommandService;

import java.util.List;

@Command(label = "example-ra")
public class ExampleRequiredArgsCommand implements CommandService {

    @Executor(pattern = "")
    private List<String> _default(@RawArgs List<String> args) {
        return args;
    }

    @Executor(pattern = {"static", "static2", "stat ic"})
    public boolean _static() {
        return true;
    }

    @Executor(pattern = "single-argument *")
    public String _static_required(@Arg String name) {
        return name;
    }

    @Executor(pattern = "two-argument * *")
    public String _static_required_required(@Arg String name1, @Arg String name2) {
        return name1 + " " + name2;
    }

    @Executor(pattern = "single-w2-argument *:2")
    public String _static_w2required(@Arg String nameAndSurname) {
        return nameAndSurname;
    }

    @Executor(pattern = "two-w2-argument *:2 *:2")
    public String _static_w2required_w2required(@Arg String nameAndSurname1, @Arg String nameAndSurname2) {
        return nameAndSurname1 + " " + nameAndSurname2;
    }
}
