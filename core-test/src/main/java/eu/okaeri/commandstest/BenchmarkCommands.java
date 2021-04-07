package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.cli.CommandsCli;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BenchmarkCommands {

    @State(Scope.Benchmark)
    public static class Data {
        public Commands commands = CommandsManager.create(new CommandsCli())
                .register(ExampleCommand.class);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    public void command_simple(Blackhole blackhole, Data data) throws InvocationTargetException, IllegalAccessException {
        blackhole.consume(data.commands.call("cmd hello siema"));
    }

    @Benchmark
    public void command_medium(Blackhole blackhole, Data data) throws InvocationTargetException, IllegalAccessException {
        blackhole.consume(data.commands.call("cmd bk xddd ddxd"));
    }

    @Benchmark
    public void command_complex(Blackhole blackhole, Data data) throws InvocationTargetException, IllegalAccessException {
        blackhole.consume(data.commands.call("cmd player Daffit set essentials.spawn true world_nether"));
    }
}
