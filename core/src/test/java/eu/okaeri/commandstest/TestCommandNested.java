package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commandstest.command.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestCommandNested {

    private Commands commands;

    @BeforeAll
    public void prepare() {
        this.commands = CommandsManager.create(new CommandsAdapter());
        this.commands.getRegistry()
                .register(ExampleWithNestedCommand.class);
    }

    @Test
    @SneakyThrows
    public void test_nested_parent_default() {
        assertEquals(0, (int) this.commands.call("example-wn"));
    }

    @Test
    @SneakyThrows
    public void test_nested_subs_default() {
        assertEquals(1, (int) this.commands.call("example-wn sub1"));
        assertEquals(2, (int) this.commands.call("example-wn sub2"));
    }

    @Test
    @SneakyThrows
    public void test_nested_subs_static() {
        assertEquals("hi", this.commands.call("example-wn sub1 some-static"));
        assertEquals("hi2", this.commands.call("example-wn sub2 some-static"));
    }
}
