package eu.okaeri.commands.brigadier;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.Completion;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.NestedCommand;
import eu.okaeri.commands.brigadier.annotation.BrigadierDisabled;
import eu.okaeri.commands.handler.access.DefaultAccessHandler;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.service.Option;
import org.junit.jupiter.api.Test;

import java.util.List;

import static eu.okaeri.commands.brigadier.BrigadierTestHarness.ASK_SERVER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public final class TestBrigadierTree {

    private static BrigadierTestHarness tree(Class<? extends CommandService> service) {
        return BrigadierTestHarness.of(service).update();
    }

    // https://github.com/OkaeriPoland/okaeri-commands/issues/7
    @Test
    public void test_sibling_patterns_all_present() {

        assertIterableEquals(asList(
            "multi something <value> something1",
            "multi something <value> something2",
            "multi something <value> something3"
        ), tree(MultiPatternCommand.class).paths());
    }

    // https://github.com/OkaeriPoland/okaeri-commands/issues/7 (comment: real-life example)
    @Test
    public void test_sibling_patterns_mixed_literal_and_argument() {

        assertIterableEquals(asList(
            "quest assign <target> <quest> <requires>",
            "quest assign @a <quest> <requires>"
        ), tree(QuestCommand.class).paths());
    }

    // https://github.com/OkaeriPoland/okaeri-commands/issues/7 (patterns of differing arity)
    @Test
    public void test_sibling_patterns_differing_arity() {

        assertIterableEquals(asList(
            "vary something <value> something1 <required1> <optional1>",
            "vary something <value> something2 <required1>"
        ), tree(VaryingArityCommand.class).paths());
    }

    @Test
    public void test_aliases_get_the_same_tree() {

        assertIterableEquals(asList(
            "ali one",
            "ali two",
            "alias one",
            "alias two"
        ), tree(AliasedCommand.class).paths());
    }

    @Test
    public void test_dynamic_completions_use_ask_server() {

        BrigadierTestHarness harness = tree(DynamicCompletionCommand.class);

        assertIterableEquals(singletonList("dyn load <name>"), harness.paths());
        assertIterableEquals(singletonList(ASK_SERVER), harness.suggest("dyn load "));
    }

    /**
     * Type derived completions are offered as literals for the client to filter
     * on its own, but the resolver behind them accepts more than they list
     * (any case), so an argument node has to sit beside them.
     */
    @Test
    public void test_enum_arguments_become_literals_plus_a_fallback() {

        BrigadierTestHarness harness = tree(EnumCommand.class);

        assertIterableEquals(asList("en mode <mode>", "en mode adventure", "en mode creative", "en mode survival"), harness.paths());
        assertIterableEquals(asList(ASK_SERVER, "adventure", "creative", "survival"), harness.suggest("en mode "));
        assertTrue(harness.executable("en mode SURVIVAL"), "the resolver is case insensitive");
    }

    @Test
    public void test_boolean_arguments_become_literals_plus_a_fallback() {

        BrigadierTestHarness harness = tree(BooleanCommand.class);

        assertIterableEquals(asList("bool flag <on>", "bool flag false", "bool flag true"), harness.paths());
        assertTrue(harness.executable("bool flag yes"), "the resolver takes yes/on/1 too");
    }

    /**
     * An explicitly written completion list is the author's choice, so it stays
     * a plain set of literals with nothing beside it.
     */
    @Test
    public void test_written_completions_get_no_fallback() {

        assertIterableEquals(asList("comp set allow", "comp set deny"), tree(CompletionCommand.class).paths());
    }

    /**
     * Brigadier has no notion of an argument spanning several inputs, so a
     * wider element takes one node per input it consumes.
     */
    @Test
    public void test_wide_elements_take_one_node_each() {

        assertIterableEquals(singletonList("wid set <coords> <coords> now"), tree(WideElementCommand.class).paths());
    }

    @Test
    public void test_nested_services_hang_off_the_root_label() {

        assertIterableEquals(asList("nest child deep <x>", "nest top"), tree(NestedParentCommand.class).paths());
    }

    @Test
    public void test_denied_executors_are_left_out() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(MultiPatternCommand.class);
        harness.commands().accessHandler(new DefaultAccessHandler() {
            @Override
            public boolean allowAccess(ExecutorMeta executor, Invocation invocation, CommandData data) {
                return !executor.getPattern().getRaw().endsWith("something2");
            }
        });

        assertIterableEquals(asList(
            "multi something <value> something1",
            "multi something <value> something3"
        ), harness.update().paths());
    }

    @Test
    public void test_services_sharing_a_label_merge_into_one_tree() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(SharedFirstCommand.class, SharedSecondCommand.class).update();

        assertIterableEquals(asList("shared first <x>", "shared second <y>"), harness.paths());
    }

    /**
     * The opt-out belongs to a service but the node belongs to the label, and
     * one node cannot be half generated, so a single opting out service keeps
     * the plain ask_server node for everything sharing that label.
     */
    @Test
    public void test_one_disabled_service_disables_the_whole_label() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(SharedFirstCommand.class, SharedDisabledCommand.class).update();

        assertIterableEquals(singletonList("shared <args>"), harness.paths());
    }

    @Test
    public void test_access_is_checked_per_service() {

        BrigadierTestHarness harness = BrigadierTestHarness.of(SharedFirstCommand.class, SharedSecondCommand.class);
        harness.commands().accessHandler(new DefaultAccessHandler() {
            @Override
            public boolean allowAccess(ServiceMeta service, Invocation invocation, CommandData data, boolean checkExecutors) {
                return !(service.getImplementor() instanceof SharedSecondCommand);
            }
        });

        assertIterableEquals(singletonList("shared first <x>"), harness.update().paths());
    }

    @Test
    public void test_disabled_service_keeps_the_generated_node() {

        BrigadierTestHarness harness = tree(DisabledCommand.class);

        assertIterableEquals(singletonList("dis <args>"), harness.paths());
        assertIterableEquals(singletonList(ASK_SERVER), harness.suggest("dis "));
    }

    @Test
    public void test_suggestions_follow_the_tree() {

        BrigadierTestHarness harness = tree(MultiPatternCommand.class);

        assertIterableEquals(singletonList("something"), harness.suggest("multi "));
        assertIterableEquals(singletonList("something"), harness.suggest("multi some"));
        assertIterableEquals(singletonList(ASK_SERVER), harness.suggest("multi something "));
        assertIterableEquals(asList("something1", "something2", "something3"), harness.suggest("multi something abc "));
        assertIterableEquals(asList("something1", "something2", "something3"), harness.suggest("multi something abc some"));
    }

    /**
     * Nodes without a command render red on the client and are reported as
     * unknown by brigadier's own parser.
     */
    @Test
    public void test_complete_paths_are_executable() {

        BrigadierTestHarness harness = tree(MultiPatternCommand.class);

        assertTrue(harness.executable("multi something abc something1"));
        assertFalse(harness.executable("multi something abc"), "the pattern is not complete yet");
    }

    @Test
    public void test_optional_elements_may_be_omitted() {

        BrigadierTestHarness harness = tree(VaryingArityCommand.class);

        assertTrue(harness.executable("vary something abc something1 req"));
        assertTrue(harness.executable("vary something abc something1 req opt"));
        assertTrue(harness.executable("vary something abc something2 req"));
    }

    /**
     * Sibling patterns naming the same argument collapse onto a single node.
     * When they disagree on the type it is dropped rather than picked, leaving
     * both the parsing and the completions to the server.
     */
    @Test
    public void test_conflicting_argument_types_fall_back_to_the_server() {

        BrigadierTestHarness harness = tree(ConflictingTypesCommand.class);

        assertTrue(harness.executable("wide set abc"), "the String branch must still parse");
        assertTrue(harness.executable("wide set 5 kg"), "the int branch must keep its children");
        assertIterableEquals(singletonList(ASK_SERVER), harness.suggest("wide set "));
    }

    // neither branch is a string, so there is no widest type to fall back to
    @Test
    public void test_conflicting_numeric_types_fall_back_to_the_server() {

        BrigadierTestHarness harness = tree(ConflictingNumbersCommand.class);

        assertTrue(harness.executable("num set 5 kg"), "the int branch must keep its children");
        assertTrue(harness.executable("num set 1.5"), "the double branch must still parse");
        assertTrue(harness.executable("num set abc"), "neither type is enforced client side anymore");
    }

    // a greedy sibling must not swallow the elements another branch still suggests
    @Test
    public void test_greedy_conflicting_with_word_keeps_the_siblings_reachable() {

        BrigadierTestHarness harness = tree(ConflictingGreedyCommand.class);

        assertIterableEquals(singletonList("say <message> loud"), harness.paths());
        assertIterableEquals(singletonList("loud"), harness.suggest("say hello "));
        assertTrue(harness.executable("say hello"));
        assertTrue(harness.executable("say hello loud"));
    }

    /**
     * Brigadier parses through the internal literals/arguments indexes, which
     * {@code getChildren().clear()} does not touch. Anything left there is
     * still matched even though it is gone from the tree.
     */
    @Test
    public void test_no_stale_nodes_left_behind() {

        BrigadierTestHarness harness = tree(MultiPatternCommand.class);

        assertIterableEquals(harness.paths(), harness.parsablePaths());
        assertFalse(harness.suggest("multi ").contains(ASK_SERVER), "the replaced greedy args node is still reachable");
    }

    @Test
    public void test_update_is_idempotent() {

        BrigadierTestHarness harness = tree(MultiPatternCommand.class);
        List<String> first = harness.paths();

        harness.update();

        assertIterableEquals(first, harness.paths());
        assertIterableEquals(first, harness.parsablePaths());
    }

    @Command(label = "multi")
    public static class MultiPatternCommand implements CommandService {

        @Executor(pattern = {"something <value> something1", "something <value> something2", "something <value> something3"})
        public String something(@Arg("value") String value) {
            return value;
        }
    }

    @Command(label = "quest")
    public static class QuestCommand implements CommandService {

        @Executor
        public String assign(@Arg("target") String target, @Arg("quest") String quest, @Arg("requires") Option<String> requires) {
            return quest;
        }

        @Executor(pattern = "assign @a <quest> <requires>")
        public String assign_all(@Arg("quest") String quest, @Arg("requires") String requires) {
            return quest;
        }
    }

    @Command(label = "vary")
    public static class VaryingArityCommand implements CommandService {

        @Executor(pattern = "something <value> something1 <required1> [optional1]")
        public String one(@Arg("value") String value, @Arg("required1") String required1, @Arg("optional1") Option<String> optional1) {
            return value;
        }

        @Executor(pattern = "something <value> something2 <required1>")
        public String two(@Arg("value") String value, @Arg("required1") String required1) {
            return value;
        }
    }

    @Command(label = "wide")
    public static class ConflictingTypesCommand implements CommandService {

        @Executor(pattern = "set <value> <unit>")
        public String withUnit(@Arg("value") int value, @Arg("unit") String unit) {
            return unit;
        }

        @Executor(pattern = "set <value>")
        public String plain(@Arg("value") String value) {
            return value;
        }
    }

    @Command(label = "num")
    public static class ConflictingNumbersCommand implements CommandService {

        @Executor(pattern = "set <value> <unit>")
        public String withUnit(@Arg("value") int value, @Arg("unit") String unit) {
            return unit;
        }

        @Executor(pattern = "set <value>")
        public String plain(@Arg("value") double value) {
            return String.valueOf(value);
        }
    }

    @Command(label = "say")
    public static class ConflictingGreedyCommand implements CommandService {

        @Executor(pattern = "<message...>")
        public String greedy(@Arg("message") String message) {
            return message;
        }

        @Executor(pattern = "<message> loud")
        public String loud(@Arg("message") String message) {
            return message;
        }
    }

    @Command(label = "alias", aliases = "ali")
    public static class AliasedCommand implements CommandService {

        @Executor(pattern = {"one", "two"})
        public String both() {
            return "ok";
        }
    }

    @Command(label = "comp")
    public static class CompletionCommand implements CommandService {

        @Executor(pattern = "set <state>")
        @Completion(arg = "state", value = {"allow", "deny"})
        public String set(@Arg("state") String state) {
            return state;
        }
    }

    @Command(label = "dyn")
    public static class DynamicCompletionCommand implements CommandService {

        @Executor(pattern = "load <name>")
        @Completion(arg = "name", value = "@scripts")
        public String load(@Arg("name") String name) {
            return name;
        }
    }

    @Command(label = "wid")
    public static class WideElementCommand implements CommandService {

        @Executor(pattern = "set <coords:2> now")
        public String set(@Arg("coords") String coords) {
            return coords;
        }
    }

    @Command(label = "nest", nested = @NestedCommand(NestedChildCommand.class))
    public static class NestedParentCommand implements CommandService {

        @Executor(pattern = "top")
        public String top() {
            return "top";
        }
    }

    @Command(label = "child")
    public static class NestedChildCommand implements CommandService {

        @Executor(pattern = "deep <x>")
        public String deep(@Arg("x") String x) {
            return x;
        }
    }

    @Command(label = "en")
    public static class EnumCommand implements CommandService {

        @Executor(pattern = "mode <mode>")
        public String mode(@Arg("mode") Mode mode) {
            return mode.name();
        }
    }

    @Command(label = "bool")
    public static class BooleanCommand implements CommandService {

        @Executor(pattern = "flag <on>")
        public String flag(@Arg("on") boolean on) {
            return String.valueOf(on);
        }
    }

    public enum Mode {
        SURVIVAL, CREATIVE, ADVENTURE
    }

    @Command(label = "shared")
    public static class SharedFirstCommand implements CommandService {

        @Executor(pattern = "first <x>")
        public String first(@Arg("x") String x) {
            return x;
        }
    }

    @Command(label = "shared")
    public static class SharedSecondCommand implements CommandService {

        @Executor(pattern = "second <y>")
        public String second(@Arg("y") String y) {
            return y;
        }
    }

    @BrigadierDisabled
    @Command(label = "shared")
    public static class SharedDisabledCommand implements CommandService {

        @Executor(pattern = "third")
        public String third() {
            return "third";
        }
    }

    @BrigadierDisabled
    @Command(label = "dis")
    public static class DisabledCommand implements CommandService {

        @Executor(pattern = "one")
        public String one() {
            return "one";
        }
    }
}
