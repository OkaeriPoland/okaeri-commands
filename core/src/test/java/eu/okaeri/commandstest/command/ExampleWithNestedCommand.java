package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.NestedCommand;
import eu.okaeri.commands.service.CommandService;

@Command(
    label = "example-wn",
    nested = {
        @NestedCommand(ExampleWithNestedCommand.Sub1.class),
        @NestedCommand(ExampleWithNestedCommand.Sub2.class)
    }
)
public class ExampleWithNestedCommand implements CommandService {

    @Executor
    public int _def() {
        return 0;
    }

    @Command(
        label = "sub1",
        nested = {@NestedCommand(Sub1.Sub1Sub1.class)}
    )
    public static class Sub1 implements CommandService {

        @Executor
        public int _sub1() {
            return 1;
        }

        @Executor(pattern = "some-static")
        public String some_static() {
            return "hi";
        }

        @Command(label = "sub1")
        public static class Sub1Sub1 implements CommandService {

            @Executor
            public int _sub11() {
                return 11;
            }
        }
    }

    @Command(label = "sub2")
    public static class Sub2 implements CommandService {

        @Executor
        public int _sub2() {
            return 2;
        }

        @Executor(pattern = "some-static")
        public String some_static() {
            return "hi2";
        }
    }
}
