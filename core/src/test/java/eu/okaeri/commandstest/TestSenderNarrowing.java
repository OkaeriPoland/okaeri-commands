package eu.okaeri.commandstest;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.exception.InvalidContextException;
import eu.okaeri.commands.handler.argument.DefaultMissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ContextMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commandstest.command.SenderNarrowingCommand;
import eu.okaeri.commandstest.sender.TestConsole;
import eu.okaeri.commandstest.sender.TestPlayer;
import eu.okaeri.commandstest.sender.TestSender;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public final class TestSenderNarrowing {

    private static final TestPlayer PLAYER = new TestPlayer("Steve");
    private static final TestConsole CONSOLE = new TestConsole();

    private OkaeriCommands commands;

    /**
     * Stands in for a platform adapter: the only thing a platform has to declare is its sender base type.
     */
    private static class TestPlatformCommands extends OkaeriCommands {
        @Override
        public Class<?> getSenderType() {
            return TestSender.class;
        }
    }

    @BeforeAll
    public void prepare() {
        this.commands = new TestPlatformCommands();
        this.commands.registerCommand(SenderNarrowingCommand.class);
    }

    private CommandData data(Object sender) {
        CommandData data = new CommandData();
        if (sender != null) {
            data.add(CommandData.SENDER, sender);
        }
        return data;
    }

    private Object call(String command, Object sender) throws Exception {
        CommandData data = this.data(sender);
        Invocation invocation = this.commands.invocationMatch(command)
            .orElseThrow(() -> new AssertionError("no executor for '" + command + "'"));
        InvocationMeta invocationMeta = this.commands.invocationPrepare(invocation, data);
        return invocationMeta.call();
    }

    private List<String> complete(String command, Object sender) {
        String[] parts = command.split(" ", 2);
        String label = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";
        List<CommandMeta> metas = this.commands.findByLabel(label);
        return this.commands.complete(metas, Invocation.of(label, args), this.data(sender));
    }

    private ContextMeta onlyContext(String command) {
        List<ContextMeta> contexts = this.commands.findByLabelAndArgs("narrow", command)
            .orElseThrow(AssertionError::new)
            .getExecutor().getContexts();
        assertEquals(1, contexts.size());
        return contexts.get(0);
    }

    @Test
    public void test_contexts_collected_without_annotation() {
        // narrowing is driven by the declared type, @Context is not required
        ContextMeta derived = this.onlyContext("player");
        assertEquals(TestPlayer.class, derived.getType());
        assertFalse(derived.isRequired());

        // annotating opts in explicitly, which is enforced even without a sender
        ContextMeta annotated = this.onlyContext("annotated");
        assertEquals(TestPlayer.class, annotated.getType());
        assertTrue(annotated.isRequired());

        // a plain @Arg executor narrows nothing
        assertTrue(this.commands.findByLabelAndArgs("narrow", "plain x").orElseThrow(AssertionError::new)
            .getExecutor().getContexts().isEmpty());
    }

    @Test
    public void test_sender_is_passed_when_type_matches() throws Exception {
        assertEquals("player:Steve", this.call("narrow player", PLAYER));
        assertEquals("console", this.call("narrow console", CONSOLE));
        // base type accepts every sender
        assertEquals("any:Steve", this.call("narrow any", PLAYER));
        assertEquals("any:CONSOLE", this.call("narrow any", CONSOLE));
    }

    @Test
    public void test_mismatched_sender_throws_instead_of_passing_null() {
        InvalidContextException exception = assertThrows(InvalidContextException.class, () -> this.call("narrow player", CONSOLE));
        assertEquals(TestPlayer.class, exception.getExpectedType());
        assertEquals(TestConsole.class, exception.getActualType());
    }

    @Test
    public void test_annotated_context_is_enforced_even_without_a_sender() {
        InvalidContextException exception = assertThrows(InvalidContextException.class, () -> this.call("narrow annotated", null));
        assertEquals(TestPlayer.class, exception.getExpectedType());
        assertNull(exception.getActualType());
    }

    /**
     * A type-derived context parameter has nothing to narrow against when the data carries no sender
     * (headless {@code Commands#call}), so it must keep falling through to the MissingArgumentHandler -
     * that is the extension point the injector module resolves DI through.
     */
    @Test
    public void test_type_derived_context_falls_through_to_the_missing_argument_handler() throws Exception {

        OkaeriCommands commands = new TestPlatformCommands();
        commands.missingArgumentHandler(new DefaultMissingArgumentHandler() {
            @Override
            public Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {
                if (TestSender.class.isAssignableFrom(param.getType())) {
                    return new TestPlayer("FromDI");
                }
                return super.resolve(invocation, data, command, param, index);
            }
        });
        commands.registerCommand(SenderNarrowingCommand.class);

        assertEquals("player:FromDI", commands.call("narrow player"));
    }

    /**
     * ...but a sender that is present and of the wrong type is a narrowing failure, not a
     * resolution gap, so the handler must not get a chance to paper over it.
     */
    @Test
    public void test_present_but_wrong_sender_is_not_delegated_to_the_handler() {

        OkaeriCommands commands = new TestPlatformCommands();
        commands.missingArgumentHandler(new DefaultMissingArgumentHandler() {
            @Override
            public Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {
                return new TestPlayer("FromDI");
            }
        });
        commands.registerCommand(SenderNarrowingCommand.class);

        CommandData data = new CommandData();
        data.add(CommandData.SENDER, CONSOLE);
        Invocation invocation = commands.invocationMatch("narrow player").orElseThrow(AssertionError::new);

        assertThrows(InvalidContextException.class, () -> commands.invocationPrepare(invocation, data).call());
    }

    public static class Injected {
    }

    @Command(label = "annotatedNonSender")
    public static class AnnotatedNonSenderCommand implements CommandService {
        @Executor
        public String go(@Context Injected injected) {
            return "go:" + ((injected == null) ? "null" : "injected");
        }
    }

    /**
     * {@code @Context} marks strictness, not membership: a type the sender could never satisfy is
     * not a context parameter at all, so it keeps reaching the MissingArgumentHandler. Otherwise the
     * annotation would break DI and report "can only be executed by Injected".
     */
    @Test
    public void test_annotation_does_not_capture_a_non_sender_type() throws Exception {

        OkaeriCommands commands = new TestPlatformCommands();
        commands.missingArgumentHandler(new DefaultMissingArgumentHandler() {
            @Override
            public Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {
                return (param.getType() == Injected.class) ? new Injected() : super.resolve(invocation, data, command, param, index);
            }
        });
        commands.registerCommand(AnnotatedNonSenderCommand.class);

        assertTrue(commands.findByLabelAndArgs("annotatedNonSender", "go").orElseThrow(AssertionError::new)
            .getExecutor().getContexts().isEmpty());
        assertEquals("go:injected", commands.call("annotatedNonSender go"));
    }

    @Test
    public void test_custom_invalid_message_is_resolved() {
        InvalidContextException exception = assertThrows(InvalidContextException.class, () -> this.call("narrow annotated", CONSOLE));
        assertEquals("players only!", exception.getMessage());
    }

    @Test
    public void test_completions_are_narrowed_to_the_sender() {
        assertIterableEquals(Arrays.asList("annotated", "any", "plain", "player"), this.complete("narrow ", PLAYER));
        assertIterableEquals(Arrays.asList("any", "console", "plain"), this.complete("narrow ", CONSOLE));
        // no sender in the context, nothing to narrow against
        assertIterableEquals(Arrays.asList("annotated", "any", "console", "plain", "player"), this.complete("narrow ", null));
    }

    @Test
    public void test_service_is_allowed_when_any_executor_accepts_the_sender() {
        assertTrue(this.commands.allowContext(this.commands.findByLabel("narrow").get(0).getService(), this.data(PLAYER)));
        assertTrue(this.commands.allowContext(this.commands.findByLabel("narrow").get(0).getService(), this.data(CONSOLE)));
        // a sender the service knows nothing about matches only the executors without narrowing
        assertTrue(this.commands.allowContext(this.commands.findByLabel("narrow").get(0).getService(), this.data("stranger")));
    }
}
