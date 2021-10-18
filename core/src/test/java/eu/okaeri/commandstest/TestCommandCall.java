package eu.okaeri.commandstest;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.service.Option;
import eu.okaeri.commandstest.command.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestCommandCall {

    private Commands commands;

    @BeforeAll
    public void prepare() {
        this.commands = new OkaeriCommands();
        this.commands
                .registerCommand(ExampleOptionalArgsCommand.class)
                .registerCommand(ExampleRequiredArgsCommand.class)
                .registerCommand(ExampleStaticArgsCommand.class)
                .registerCommand(SimpleOptionalArgsCommand.class)
                .registerCommand(SimpleRequiredArgsCommand.class)
                .registerCommand(SimpleTrickyCommand.class);
    }

    @Test
    @SneakyThrows
    public void test_default_empty() {
        assertEquals(0, ((List<?>) this.commands.call("example-ra")).size());
    }

    @Test
    public void test_unknown_command() {
        assertTrue(this.commands.findByLabel("some-unknown-command").isEmpty());
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("some-unknown-command"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("some-unknown-command a b c"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("some-unknown-command 1 2 3"));
    }

    @Test
    public void test_unknown_command_pattern() {
        assertFalse(this.commands.findByLabel("example-sa").isEmpty());
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-sa unknown-pattern"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-sa unknown-pattern a b c"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-sa unknown-pattern 1 2 3"));
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
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("simple-ra"));
    }

    @Test
    @SneakyThrows
    public void test_required_required() {
        assertEquals("Heh1 Heh2", this.commands.call("simple-ra Heh1 Heh2"));
        assertEquals("ItWorks!1 ItWorks!2", this.commands.call("simple-ra ItWorks!1 ItWorks!2"));
    }

    @Test
    public void test_required_required_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("simple-ra Heh1 Heh2 ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("simple-ra ItWorks!1 ItWorks!2 ??? ???"));
    }

    @Test
    @SneakyThrows
    public void test_optional() {
        assertEquals(Option.of("Heh"), this.commands.call("simple-oa Heh"));
        assertEquals(Option.of("ItWorks!"), this.commands.call("simple-oa ItWorks!"));
    }

    @Test
    public void test_optional_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("simple-oa Heh abc"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("simple-oa ItWorks! abc"));
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
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument Player ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument unknown ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument null funny but it does not work that way"));
    }

    @Test
    public void test_static_required_missing() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_consumingrequired() {
        assertEquals("John", this.commands.call("example-ra consuming-argument John"));
        assertEquals("John Doe", this.commands.call("example-ra consuming-argument John Doe"));
        assertEquals("John Doe Jan", this.commands.call("example-ra consuming-argument John Doe Jan"));
        assertEquals("John Doe Jan Kowalski", this.commands.call("example-ra consuming-argument John Doe Jan Kowalski"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_consumingrequired() {
        assertIterableEquals(Arrays.asList("Takashi Yamada", "John"), this.commands.call("example-ra w2-and-consuming-argument Takashi Yamada John"));
        assertIterableEquals(Arrays.asList("Takashi Yamada", "John Doe"), this.commands.call("example-ra w2-and-consuming-argument Takashi Yamada John Doe"));
        assertIterableEquals(Arrays.asList("Takashi Yamada", "John Doe Jan"), this.commands.call("example-ra w2-and-consuming-argument Takashi Yamada John Doe Jan"));
        assertIterableEquals(Arrays.asList("Takashi Yamada", "John Doe Jan Kowalski"), this.commands.call("example-ra w2-and-consuming-argument Takashi Yamada John Doe Jan Kowalski"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required() {
        assertEquals("John Doe", this.commands.call("example-ra single-w2-argument John Doe"));
        assertEquals("Jan Kowalski", this.commands.call("example-ra single-w2-argument Jan Kowalski"));
    }

    @Test
    public void test_static_w2required_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John Doe ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John Doe ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John Doe funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_partial() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument John  "));
    }

    @Test
    public void test_static_w2required_missing() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra single-w2-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_w2required() {
        assertEquals("John Doe Jan Kowalski", this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski"));
        assertEquals("Jan Kowalski John Doe", this.commands.call("example-ra two-w2-argument Jan Kowalski John Doe"));
    }

    @Test
    public void test_static_w2required_w2required_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan Kowalski funny but it does not work that way"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2required_w2required_partial() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-w2-argument John Doe Jan  "));
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
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument Player1 Player2 ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument unknown1 unknown2 ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument null null funny but it does not work that way"));
    }

    @Test
    public void test_static_required_required_missing_single() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument Player1"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument Player1 "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument Player1  "));
    }

    @Test
    public void test_static_required_required_missing_all() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument "));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-ra two-argument  "));
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
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-argument Player ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-argument unknown ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-argument null funny but it does not work that way"));
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
    public void test_static_consumingoptional() {
        assertEquals(Option.of("John"), this.commands.call("example-oa consuming-argument John"));
        assertEquals(Option.of("John Doe"), this.commands.call("example-oa consuming-argument John Doe"));
        assertEquals(Option.of("John Doe Jan"), this.commands.call("example-oa consuming-argument John Doe Jan"));
        assertEquals(Option.of("John Doe Jan Kowalski"), this.commands.call("example-oa consuming-argument John Doe Jan Kowalski"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_consumingoptional() {
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of("John")), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada John"));
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of("John Doe")), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada John Doe"));
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of("John Doe Jan")), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada John Doe Jan"));
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of("John Doe Jan Kowalski")), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada John Doe Jan Kowalski"));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional_consumingoptional_only_first() {
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of(null)), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada"));
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of(null)), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada "));
        assertIterableEquals(Arrays.asList(Option.of("Takashi Yamada"), Option.of(null)), this.commands.call("example-oa w2-and-consuming-argument Takashi Yamada  "));
    }

    @Test
    @SneakyThrows
    public void test_static_w2optional() {
        assertEquals(Option.of("John Doe"), this.commands.call("example-oa single-w2-argument John Doe"));
        assertEquals(Option.of("Jan Kowalski"), this.commands.call("example-oa single-w2-argument Jan Kowalski"));
    }

    @Test
    public void test_static_w2optional_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-w2-argument John Doe ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-w2-argument John Doe ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa single-w2-argument John Doe funny but it does not work that way"));
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
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-w2-argument John Doe Jan Kowalski funny but it does not work that way"));
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
    @SneakyThrows
    public void test_static_optional_optional_java() {
        assertIterableEquals(Arrays.asList(Optional.of("Player1"), Optional.of("Player2")), this.commands.call("example-oa java-two-argument Player1 Player2"));
        assertEquals(Arrays.asList(Optional.of("unknown1"), Optional.of("unknown2")), this.commands.call("example-oa java-two-argument unknown1 unknown2"));
        assertEquals(Arrays.asList(Optional.of("null"), Optional.of("null")), this.commands.call("example-oa java-two-argument null null"));
    }

    @Test
    public void test_static_optional_optional_too_long() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-argument Player1 Player2 ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-argument unknown1 unknown2 ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa two-argument null null funny but it does not work that way"));
    }

    @Test
    public void test_static_optional_optional_too_long_java() {
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa java-two-argument Player1 Player2 ???"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa java-two-argument unknown1 unknown2 ? ? ?"));
        assertThrows(NoSuchCommandException.class, () -> this.commands.call("example-oa java-two-argument null null funny but it does not work that way"));
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
    public void test_static_optional_optional_missing_single_java() {
        assertIterableEquals(Arrays.asList(Optional.of("Player1"), Optional.empty()), this.commands.call("example-oa java-two-argument Player1"));
        assertIterableEquals(Arrays.asList(Optional.of("Player1"), Optional.empty()), this.commands.call("example-oa java-two-argument Player1 "));
        assertIterableEquals(Arrays.asList(Optional.of("Player1"), Optional.empty()), this.commands.call("example-oa java-two-argument Player1  "));
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
    public void test_static_optional_optional_missing_all_java() {
        assertIterableEquals(Arrays.asList(Optional.empty(), Optional.empty()), this.commands.call("example-oa java-two-argument"));
        assertIterableEquals(Arrays.asList(Optional.empty(), Optional.empty()), this.commands.call("example-oa java-two-argument "));
        assertIterableEquals(Arrays.asList(Optional.empty(), Optional.empty()), this.commands.call("example-oa java-two-argument  "));
    }

    @Test
    @SneakyThrows
    public void test_tricky_command() {
        assertEquals("lists", this.commands.call("tricky list"));
        assertEquals("lists", this.commands.call("tricky lists"));
        assertEquals("hi", this.commands.call("tricky hi"));
    }
}
