package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.service.CommandService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An element wider than one consumes several arguments, so the position in the
 * pattern and the position in the arguments drift apart. Matching used to count
 * elements while indexing arguments, which both mismatched and, once the drift
 * ran past the end, threw out of the command handler.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestCommandWideArgs {

    private Commands commands;

    @BeforeAll
    public void prepare() {
        this.commands = new OkaeriCommands();
        this.commands.registerCommand(WideArgsCommand.class);
    }

    @Test
    public void test_match_full_width() {
        assertTrue(this.commands.invocationMatch("wide set 1 2 now").isPresent());
    }

    @Test
    public void test_match_rejects_partial_width() {
        assertFalse(this.commands.invocationMatch("wide set 1 now").isPresent());
        assertFalse(this.commands.invocationMatch("wide set 1 2").isPresent());
        assertFalse(this.commands.invocationMatch("wide set 1").isPresent());
        assertFalse(this.commands.invocationMatch("wide set 1 2 now extra").isPresent());
    }

    @Test
    public void test_complete_inside_a_wide_element() {
        // the second half of <coords:2>, not the "now" that follows it
        assertIterableEquals(Collections.emptyList(), this.commands.complete("wide set 1 "));
        assertIterableEquals(Collections.singletonList("now"), this.commands.complete("wide set 1 2 "));
    }

    @Test
    public void test_wide_value_is_joined() throws Exception {
        assertEquals("1 2", this.commands.call("wide set 1 2 now"));
    }

    @Test
    public void test_trailing_wide_element() throws Exception {
        assertEquals("a b c", this.commands.call("wide tail a b c"));
        assertFalse(this.commands.invocationMatch("wide tail a b").isPresent());
    }

    @Command(label = "wide")
    public static class WideArgsCommand implements CommandService {

        @Executor(pattern = "set <coords:2> now")
        public String set(@Arg("coords") String coords) {
            return coords;
        }

        @Executor(pattern = "tail <rest:3>")
        public String tail(@Arg("rest") String rest) {
            return rest;
        }
    }
}
