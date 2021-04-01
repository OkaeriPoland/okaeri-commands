package eu.okaeri.commandstest;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Option;

import java.util.Arrays;
import java.util.List;

@ServiceDescriptor(label = "cmd", description = "Example command service")
public class ExampleCommand implements CommandService {

    @Executor(fallback = true)
    private void defaultExecutor(@RawArgs List<String> args) {
        System.out.println("called default method: " + args);
    }

    // cmd hello
    // cmd hello siema
    // cmd siema
    @Executor(pattern = {"hello", "siema", "hello siema"}, description = "Prints hello message")
    public void testHello() {
        System.out.println("HELLO!");
    }

    // cmd hi
    // cmd hii
    // overwrite usage 'cmd <hi|hi> with custom key processed by CommandsAdapter#resolveText()
    @Executor(pattern = {"hi", "hii"}, description = "Prints hello message", usage = "!command-cmd-test-hi-usage")
    public void testHi() {
        System.out.println("Hi!");
    }

    // cmd hello <name>
    @Executor(pattern = "hello <name>", description = "Prints hello message with name")
    public void testHelloParam(@Arg("name") String name) {
        System.out.println("HELLO " + name + "!");
    }

    // cmd bk [name]
    @Executor(pattern = "bk [name]", description = "Prints beka message with name")
    public void testOptionalParam(@Arg("name") Option<String> name) {
        System.out.println("beka z " + name + "!");
    }

    // cmd test
    @Executor(description = "Prints test message")
    public void test() {
        System.out.println("TEST!");
    }

    // cmd ping
    @Executor(description = "Responds to pings")
    public void ping() {
        System.out.println("PONG!");
    }

    // elo
    @Executor(pattern = "player <player> set <perm> <value> [flag]", description = "Complex command test")
    public String testPermExample(@Arg("player") String name, @Arg("perm") String perm, @Arg("value") String value, @Arg("flag") Option<String> flag) {
        return (name + " " + perm + " " + value + " " + flag);
    }

    // elo
    @Executor(pattern = "player <arg0> set <arg1> <arg2> [arg3]", description = "Complex command test")
    public String testPermExample2(@Arg String name, @Arg String perm, @Arg String value, @Arg Option<String> flag) {
        return (name + " " + perm + " " + value + " " + flag);
    }

    // elo
    @Executor(pattern = "player * sett * * ?", description = "Complex command test")
    public String testPermExample3(@Arg String name, @Arg String perm, @RawArgs String[] args, @Arg String value, @Arg Option<String> flag) {
        return ("kozak " + name + " " + perm + " " + value + " " + flag + "\n" + Arrays.toString(args));
    }
}
