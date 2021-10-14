package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestCommandComplete {

    private Commands commands;

    @BeforeAll
    public void prepare() {
        this.commands = CommandsManager.create(new CommandsAdapter());
        this.commands.getRegistry()
                .register(TabCompleteCommand.class);
    }

    @Command(label = "tab1")
    public static class TabCompleteCommand implements CommandService {

        @Executor(pattern = {"static", "static2", "stat ic"})
        public boolean _static() {
            return true;
        }
    }

    @Test
    public void test_complete_1st_arg_static() {
        assertIterableEquals(Arrays.asList("stat", "static", "static2"), this.commands.complete("tab1 s"));
        assertIterableEquals(Arrays.asList("stat", "static", "static2"), this.commands.complete("tab1 st"));
        assertIterableEquals(Arrays.asList("stat", "static", "static2"), this.commands.complete("tab1 sta"));
        assertIterableEquals(Arrays.asList("stat", "static", "static2"), this.commands.complete("tab1 stat"));
        assertIterableEquals(Arrays.asList("static", "static2"), this.commands.complete("tab1 stati"));
        assertIterableEquals(Arrays.asList("static", "static2"), this.commands.complete("tab1 static"));
        assertIterableEquals(Collections.singletonList("static2"), this.commands.complete("tab1 static2"));
        assertIterableEquals(Collections.singletonList("ic"), this.commands.complete("tab1 stat "));
        assertIterableEquals(Collections.singletonList("ic"), this.commands.complete("tab1 stat i"));
        assertIterableEquals(Collections.singletonList("ic"), this.commands.complete("tab1 stat ic"));
    }

    @Test
    public void test_complete_1st_arg_static_should_not_complete() {
        assertIterableEquals(Collections.emptyList(), this.commands.complete("tab1 static "));
        assertIterableEquals(Collections.emptyList(), this.commands.complete("tab1 static  "));
    }
}
