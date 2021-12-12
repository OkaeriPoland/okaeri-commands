package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.*;
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
        this.commands = new OkaeriCommands();
        this.commands.registerCommand(TabCompleteCommand.class);
        this.commands.registerCompletion("scripts", (completion, argument, invocationContext, commandContext) -> Arrays.asList("script.py", "script.groovy"));
    }

    @Test
    public void test_complete_1static() {
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
        assertIterableEquals(Collections.emptyList(), this.commands.complete("tab1 static "));
        assertIterableEquals(Collections.emptyList(), this.commands.complete("tab1 static  "));
    }

    @Test
    public void test_complete_1static_1required() {
        assertIterableEquals(Collections.singletonList("load"), this.commands.complete("tab1 l"));
        assertIterableEquals(Collections.singletonList("load"), this.commands.complete("tab1 lo"));
        assertIterableEquals(Collections.singletonList("load"), this.commands.complete("tab1 loa"));
        assertIterableEquals(Collections.singletonList("load"), this.commands.complete("tab1 load"));
        assertIterableEquals(Arrays.asList("script.py", "script.groovy"), this.commands.complete("tab1 load "));
        assertIterableEquals(Arrays.asList("script.py", "script.groovy"), this.commands.complete("tab1 load  "));
        assertIterableEquals(Arrays.asList("allow", "deny"), this.commands.complete("tab1 updateState "));
    }

    @Command(label = "tab1")
    public static class TabCompleteCommand implements CommandService {

        @Executor(pattern = {"static", "static2", "stat ic"})
        public boolean _static() {
            return true;
        }

        @Executor(pattern = "load *")
        @Completions(@Completion(arg = "name", value = "@scripts"))
        public boolean _load(@Arg String name) {
            return true;
        }

        @Executor(pattern = "updateState *")
        @Completions(@Completion(arg = "state", value = {"allow", "deny"}))
        public String _state(@Arg String state) {
            return state;
        }
    }
}
