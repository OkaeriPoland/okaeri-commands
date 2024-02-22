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
        this.commands.registerCompletion("scripts", (completion, argument, invocation, data) -> Arrays.asList("script.py", "script.groovy"));
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
        assertIterableEquals(Arrays.asList("script.groovy", "script.py"), this.commands.complete("tab1 load "));
        assertIterableEquals(Arrays.asList("script.groovy", "script.py"), this.commands.complete("tab1 load  "));
        assertIterableEquals(Arrays.asList("allow", "deny"), this.commands.complete("tab1 updateState "));
        assertIterableEquals(Collections.singletonList("allow"), this.commands.complete("tab1 updateState a"));
        assertIterableEquals(Collections.singletonList("allow"), this.commands.complete("tab1 updateState allow"));
    }

    @Test
    public void test_complete_1static_1required_1static() {
        assertIterableEquals(Arrays.asList("player1", "player2"), this.commands.complete("tab1 join pla"));
        assertIterableEquals(Collections.singletonList("player1"), this.commands.complete("tab1 join player1"));
        assertIterableEquals(Arrays.asList("ask", "force"), this.commands.complete("tab1 join player1 "));
        assertIterableEquals(Collections.singletonList("force"), this.commands.complete("tab1 join player1 f"));
        assertIterableEquals(Collections.singletonList("ask"), this.commands.complete("tab1 join player1 a"));
    }

    @Command(label = "tab1")
    public static class TabCompleteCommand implements CommandService {

        @Executor(pattern = {"static", "static2", "stat ic"})
        public boolean _static() {
            return true;
        }

        @Executor(pattern = "load *")
        @Completion(arg = "name", value = "@scripts")
        public boolean _load(@Arg String name) {
            return true;
        }

        @Executor(pattern = "updateState *")
        @Completion(arg = "state", value = {"allow", "deny"})
        public String _state(@Arg String state) {
            return state;
        }

        @Executor(pattern = "join * force")
        @Completion(arg = "player", value = {"player1", "player2"})
        public String _join_force(@Arg String player) {
            return player;
        }

        @Executor(pattern = "join * ask")
        @Completion(arg = "player", value = {"player1", "player2"})
        public String _join_ask(@Arg String player) {
            return player;
        }
    }
}
