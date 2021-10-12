package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.service.Option;
import eu.okaeri.commandstest.command.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestCommandCall {

    private Commands commands;

    @BeforeAll
    public void prepare() {
        this.commands = CommandsManager.create(new CommandsAdapter());
        this.commands.getRegistry()
                .register(ExampleOptionalArgsCommand.class)
                .register(ExampleRequiredArgsCommand.class)
                .register(ExampleStaticArgsCommand.class)
                .register(SimpleOptionalArgsCommand.class)
                .register(SimpleRequiredArgsCommand.class)
                .register(SimpleTrickyCommand.class);
    }

    @Test
    @SneakyThrows
    public void test_default_empty() {
        assertEquals(0, ((List<?>) this.commands.call("example-ra")).size());
    }

    @Test
    public void test_unknown_command() {
        assertTrue(this.commands.getRegistry().findByLabel("some-unknown-command").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("some-unknown-command"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("some-unknown-command a b c"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("some-unknown-command 1 2 3"));
    }

    @Test
    public void test_unknown_command_pattern() {
        assertFalse(this.commands.getRegistry().findByLabel("example-sa").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-sa unknown-pattern"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-sa unknown-pattern a b c"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-sa unknown-pattern 1 2 3"));
    }

    @Test
    @SneakyThrows
    public void test_static() {
        assertEquals(true, this.commands.call("example-ra static"));
        assertEquals(true, this.commands.call("example-ra static2"));
        assertEquals(true, this.commands.call("example-ra stat ic"));

        assertEquals(true, this.commands.call("example-oa static"));
        assertEquals(true, this.commands.call("example-oa static2"));
        assertEquals(true, this.commands.call("example-oa stat ic"));
    }

    @Test
    @SneakyThrows
    public void test_required() {
        assertEquals("Heh", this.commands.call("simple-ra Heh"));
        assertEquals("ItWorks!", this.commands.call("simple-ra ItWorks!"));
    }

    @Test
    public void test_required_missing() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("simple-ra"));
    }

    @Test
    @SneakyThrows
    public void test_required_required() {
        assertEquals("Heh1 Heh2", this.commands.call("simple-ra Heh1 Heh2"));
        assertEquals("ItWorks!1 ItWorks!2", this.commands.call("simple-ra ItWorks!1 ItWorks!2"));
    }

    @Test
    public void test_required_required_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("simple-ra Heh1 Heh2 ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("simple-ra ItWorks!1 ItWorks!2 ??? ???"));
    }

    @Test
    @SneakyThrows
    public void test_optional() {
        assertEquals(Option.of("Heh"), this.commands.call("simple-oa Heh"));
        assertEquals(Option.of("ItWorks!"), this.commands.call("simple-oa ItWorks!"));
    }

    @Test
    public void test_optional_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("simple-oa Heh abc"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("simple-oa ItWorks! abc"));
    }

    @Test
    @SneakyThrows
    public void test_optional_missing() {
        assertEquals(Option.of(null), this.commands.call("simple-oa"));
    }

    @Test
    @SneakyThrows
    public void test_static_required() {
        assertEquals("Player", this.commands.call("example-ra single-argument Player"));
        assertEquals("unknown", this.commands.call("example-ra single-argument unknown"));
        assertEquals("null", this.commands.call("example-ra single-argument null"));
    }

    @Test
    public void test_static_required_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument Player ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument unknown ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument null funny but it does not work that way"));
    }

    @Test
    public void test_static_required_missing() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required() {
        assertEquals("John Doe", this.commands.call("example-ra single-w2-argument John Doe"));
        assertEquals("Jan Kowalski", this.commands.call("example-ra single-w2-argument Jan Kowalski"));
    }

    @Test
    public void test_static_w2required_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John Doe ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John Doe ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John Doe funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_partial() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument John  "));
    }

    @Test
    public void test_static_w2required_missing() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra single-w2-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_w2required() {
        assertEquals("John Doe Jan Kowalski", this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski"));
        assertEquals("Jan Kowalski John Doe", this.commands.call("example-ra two-w2-argument Jan Kowalski John Doe"));
    }

    @Test
    public void test_static_w2required_w2required_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_w2required_partial() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan  "));
    }

    @Test
    @SneakyThrows
    public void test_static_required_required() {
        assertEquals("Player1 Player2", this.commands.call("example-ra two-argument Player1 Player2"));
        assertEquals("unknown1 unknown2", this.commands.call("example-ra two-argument unknown1 unknown2"));
        assertEquals("null null", this.commands.call("example-ra two-argument null null"));
    }

    @Test
    public void test_static_required_required_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument Player1 Player2 ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument unknown1 unknown2 ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument null null funny but it does not work that way"));
    }

    @Test
    public void test_static_required_required_missing_single() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument Player1"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument Player1 "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument Player1  "));
    }

    @Test
    public void test_static_required_required_missing_all() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument "));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-ra two-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_optional() {
        assertEquals(Option.of("Player"), this.commands.call("example-oa single-argument Player"));
        assertEquals(Option.of("unknown"), this.commands.call("example-oa single-argument unknown"));
        assertEquals(Option.of("null"), this.commands.call("example-oa single-argument null"));
    }

    @Test
    public void test_static_optional_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-argument Player ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-argument unknown ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-argument null funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_optional_missing() {
        assertEquals(Option.of(null), this.commands.call("example-oa single-argument"));
        assertEquals(Option.of(null), this.commands.call("example-oa single-argument "));
        assertEquals(Option.of(null), this.commands.call("example-oa single-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional() {
        assertEquals(Option.of("John Doe"), this.commands.call("example-oa single-w2-argument John Doe"));
        assertEquals(Option.of("Jan Kowalski"), this.commands.call("example-oa single-w2-argument Jan Kowalski"));
    }

    @Test
    public void test_static_w2optional_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-w2-argument John Doe ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-w2-argument John Doe ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa single-w2-argument John Doe funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_partial() {
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument John"));
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument John "));
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument John  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_missing() {
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument"));
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument "));
        assertEquals(Option.of(null), this.commands.call("example-oa single-w2-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_w2optional() {
        assertIterableEquals(Arrays.asList(Option.of("John Doe"), Option.of("Jan Kowalski")), this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski"));
        assertIterableEquals(Arrays.asList(Option.of("Jan Kowalski"), Option.of("John Doe")), this.commands.call("example-oa two-w2-argument Jan Kowalski John Doe"));
    }

    @Test
    public void test_static_w2optional_w2optional_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_w2optional_partial() {
        assertIterableEquals(Arrays.asList(Option.of("John Doe"), Option.of(null)), this.commands.call("example-oa two-w2-argument John Doe Jan"));
        assertIterableEquals(Arrays.asList(Option.of("John Doe"), Option.of(null)), this.commands.call("example-oa two-w2-argument John Doe Jan "));
        assertIterableEquals(Arrays.asList(Option.of("John Doe"), Option.of(null)), this.commands.call("example-oa two-w2-argument John Doe Jan  "));
    }

    @Test
    @SneakyThrows
    public void test_static_optional_optional() {
        assertIterableEquals(Arrays.asList(Option.of("Player1"), Option.of("Player2")), this.commands.call("example-oa two-argument Player1 Player2"));
        assertEquals(Arrays.asList(Option.of("unknown1"), Option.of("unknown2")), this.commands.call("example-oa two-argument unknown1 unknown2"));
        assertEquals(Arrays.asList(Option.of("null"), Option.of("null")), this.commands.call("example-oa two-argument null null"));
    }

    @Test
    public void test_static_optional_optional_too_long() {
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-argument Player1 Player2 ???"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-argument unknown1 unknown2 ? ? ?"));
        assertThrows(IllegalArgumentException.class, () -> this.commands.call("example-oa two-argument null null funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_optional_optional_missing_single() {
        assertIterableEquals(Arrays.asList(Option.of("Player1"), Option.of(null)), this.commands.call("example-oa two-argument Player1"));
        assertIterableEquals(Arrays.asList(Option.of("Player1"), Option.of(null)), this.commands.call("example-oa two-argument Player1 "));
        assertIterableEquals(Arrays.asList(Option.of("Player1"), Option.of(null)), this.commands.call("example-oa two-argument Player1  "));
    }

    @Test
    @SneakyThrows
    public void test_static_optional_optional_missing_all() {
        assertIterableEquals(Arrays.asList(Option.of(null), Option.of(null)), this.commands.call("example-oa two-argument"));
        assertIterableEquals(Arrays.asList(Option.of(null), Option.of(null)), this.commands.call("example-oa two-argument "));
        assertIterableEquals(Arrays.asList(Option.of(null), Option.of(null)), this.commands.call("example-oa two-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_tricky_command() {
        assertEquals("lists", this.commands.call("tricky list"));
        assertEquals("lists", this.commands.call("tricky lists"));
        assertEquals("hi", this.commands.call("tricky hi"));
    }
}
