package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Option;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.okaeri.commands.annotation.Arg.NULL;

@Command(label = "example-oa")
public class ExampleOptionalArgsCommand implements CommandService {

    @Executor
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

    @Executor(pattern = "single-argument-nullable ?")
    public Object _static_optionalrawnull(@Arg(or = NULL) String name) {
        return name;
    }

    @Executor(pattern = "single-argument-or ?")
    public Object _static_optionalrawdef(@Arg(or = "def") String name) {
        return name;
    }

    @Executor(pattern = "single-argument-oro ?")
    public Object _static_optionaloptdef(@Arg(or = "def") Option<String> name) {
        return name;
    }

    @Executor(pattern = "two-argument ? ?")
    public Object _static_optional_optional(@Arg Option<String> name1, @Arg Option<String> name2) {
        return Arrays.asList(name1, name2);
    }

    @Executor(pattern = "java-two-argument ? ?")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Object _static_joptional_joptional(@Arg Optional<String> name1, @Arg Optional<String> name2) {
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

    @Executor(pattern = "consuming-argument <longName...>")
    public Object _static_consuming(@Arg Option<String> longName) {
        return longName;
    }

    @Executor(pattern = "w2-and-consuming-argument [nameAndUsername:2] ?...")
    public Object _static_w2optional_consuming(@Arg Option<String> nameAndUsername, @Arg Option<String> longName) {
        return Arrays.asList(nameAndUsername, longName);
    }
}
