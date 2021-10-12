package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Option;

import java.util.Arrays;
import java.util.List;

@Command(label = "example-oa")
public class ExampleOptionalArgsCommand implements CommandService {

    @Executor(pattern = "")
    private List<String> _default(@RawArgs List<String> args) {
        return args;
    }

    @Executor(pattern = {"static", "static2", "stat ic"})
    public boolean _static() {
        return true;
    }

    @Executor(pattern = "single-argument ?")
    public Object _static_optional(@Arg Option<String> name) {
        return name;
    }

    @Executor(pattern = "two-argument ? ?")
    public Object _static_optional_optional(@Arg Option<String> name1, @Arg Option<String> name2) {
        return Arrays.asList(name1, name2);
    }

    @Executor(pattern = "single-w2-argument ?:2")
    public Object _static_w2optional(@Arg Option<String> nameAndSurname) {
        return nameAndSurname;
    }

    @Executor(pattern = "two-w2-argument ?:2 ?:2")
    public Object _static_w2optional_w2optional(@Arg Option<String> nameAndSurname1, @Arg Option<String> nameAndSurname2) {
        return Arrays.asList(nameAndSurname1, nameAndSurname2);
    }
}
