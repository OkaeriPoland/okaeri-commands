package eu.okaeri.commands.brigadier;

import eu.okaeri.commands.service.CommandService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static eu.okaeri.commands.brigadier.BrigadierTestHarness.ASK_SERVER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The brigadier tree is a client-side model of what the server will accept.
 * Where the two disagree the player is told a command is wrong when it is not,
 * or the other way around, so every shape covered by {@link TestBrigadierTree}
 * is checked against the engine that actually runs it.
 */
public final class TestBrigadierParity {

    private static void assertAccepts(Class<? extends CommandService> service, String... inputs) {
        assertAccepts(BrigadierTestHarness.of(service).update(), inputs);
    }

    private static void assertAccepts(BrigadierTestHarness harness, String... inputs) {
        for (String input : inputs) {
            assertEquals(harness.commands().invocationMatch(input).isPresent(), harness.executable(input),
                "client and server disagree about '" + input + "'");
        }
    }

    private static void assertSuggests(Class<? extends CommandService> service, String... inputs) {
        assertSuggests(BrigadierTestHarness.of(service).update(), inputs);
    }

    private static void assertSuggests(BrigadierTestHarness harness, String... inputs) {
        for (String input : inputs) {

            List<String> server = harness.commands().complete(input);
            List<String> client = harness.suggest(input);

            // an ask_server node means the client defers for this argument, so
            // whatever it offers by itself only has to be part of the answer
            if (client.remove(ASK_SERVER)) {
                assertTrue(server.containsAll(client),
                    "client offers completions the server does not for '" + input + "': " + client + " vs " + server);
                continue;
            }

            assertIterableEquals(server, client, "client and server disagree about completions for '" + input + "'");
        }
    }

    @Test
    public void test_sibling_patterns() {
        assertAccepts(TestBrigadierTree.MultiPatternCommand.class,
            "multi", "multi something", "multi something abc",
            "multi something abc something1", "multi something abc something4", "multi something abc something1 extra");
    }

    @Test
    public void test_mixed_literal_and_argument() {
        assertAccepts(TestBrigadierTree.QuestCommand.class,
            "quest", "quest assign", "quest assign player1", "quest assign player1 q1",
            "quest assign player1 q1 q2", "quest assign @a q1 q2");
    }

    /**
     * Known divergence. Brigadier matches a literal before any sibling argument
     * and never backtracks, so an input equal to another pattern's literal can
     * only be read as that pattern. The server tries every pattern and still
     * reaches the argument, which makes the client the stricter of the two.
     */
    @Test
    public void test_literal_shadows_a_sibling_argument() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(TestBrigadierTree.QuestCommand.class).update();

        assertTrue(harness.commands().invocationMatch("quest assign @a q1").isPresent(), "the server reads @a as the target");
        assertFalse(harness.executable("quest assign @a q1"), "the client can only read @a as the literal");
    }

    /**
     * Known divergence. A greedy element next to a single word one for the same
     * argument collapses to a word, so the client stops accepting the rest of
     * the line the greedy branch would have taken.
     */
    @Test
    public void test_greedy_conflict_narrows_the_client() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(TestBrigadierTree.ConflictingGreedyCommand.class).update();

        assertTrue(harness.commands().invocationMatch("say hello world").isPresent(), "the server takes the whole message");
        assertFalse(harness.executable("say hello world"), "the client stops after one word");
    }

    @Test
    public void test_differing_arity() {
        assertAccepts(TestBrigadierTree.VaryingArityCommand.class,
            "vary something abc something1", "vary something abc something1 req", "vary something abc something1 req opt",
            "vary something abc something2", "vary something abc something2 req", "vary something abc something2 req extra");
    }

    @Test
    public void test_aliases() {
        assertAccepts(TestBrigadierTree.AliasedCommand.class, "alias one", "ali one", "alias three");
    }

    @Test
    public void test_conflicting_types() {
        assertAccepts(TestBrigadierTree.ConflictingTypesCommand.class,
            "wide set abc", "wide set 5", "wide set 5 kg", "wide set abc kg", "wide set 5 kg extra");
    }

    @Test
    public void test_conflicting_numeric_types() {
        assertAccepts(TestBrigadierTree.ConflictingNumbersCommand.class,
            "num set 5", "num set 1.5", "num set abc", "num set 5 kg");
    }

    @Test
    public void test_wide_elements() {
        assertAccepts(TestBrigadierTree.WideElementCommand.class,
            "wid set 1 2 now", "wid set 1 now", "wid set 1", "wid set 1 2", "wid set 1 2 now extra");
    }

    @Test
    public void test_nested_services() {
        assertAccepts(TestBrigadierTree.NestedParentCommand.class,
            "nest top", "nest child deep x", "nest child deep", "nest child", "nest nope");
    }

    @Test
    public void test_services_sharing_a_label() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(
            TestBrigadierTree.SharedFirstCommand.class, TestBrigadierTree.SharedSecondCommand.class).update();

        assertAccepts(harness, "shared first a", "shared second b", "shared first", "shared third c");
        assertSuggests(harness, "shared ", "shared f");
    }

    @Test
    public void test_static_completions_match_the_server() {
        assertSuggests(TestBrigadierTree.CompletionCommand.class, "comp set ", "comp set a");
    }

    @Test
    public void test_enum_completions_match_the_server() {
        assertSuggests(TestBrigadierTree.EnumCommand.class, "en mode ", "en mode s");
        assertAccepts(TestBrigadierTree.EnumCommand.class, "en mode survival", "en mode SURVIVAL", "en mode nope", "en mode");
    }

    @Test
    public void test_boolean_completions_match_the_server() {
        assertSuggests(TestBrigadierTree.BooleanCommand.class, "bool flag ", "bool flag t");
        assertAccepts(TestBrigadierTree.BooleanCommand.class, "bool flag true", "bool flag YES", "bool flag 1", "bool flag");
    }
}
