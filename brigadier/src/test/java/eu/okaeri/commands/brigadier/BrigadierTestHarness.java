package eu.okaeri.commands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * Test-only stand-in for the Paper integration. Builds the same
 * {@link RootCommandNode} shape that CraftBukkit hands to
 * AsyncPlayerSendCommandsEvent (a label literal with a single greedy
 * "args" child suggesting ask_server) and then runs the real
 * {@link CommandsBrigadierBase#update} against it.
 */
final class BrigadierTestHarness extends CommandsBrigadierBase {

    static final String ASK_SERVER = "<ask_server>";

    private static final SuggestionProvider<Object> ASK_SERVER_SUGGESTION =
        (context, builder) -> builder.suggest(ASK_SERVER).buildFuture();

    private final RootCommandNode<Object> root = new RootCommandNode<>();
    private final CommandData data = new CommandData();

    private BrigadierTestHarness(OkaeriCommands commands) {
        this.commands = commands;
    }

    @SafeVarargs
    static BrigadierTestHarness of(Class<? extends CommandService>... services) {
        OkaeriCommands commands = new OkaeriCommands();
        for (Class<? extends CommandService> service : services) {
            commands.registerCommand(service);
        }
        return new BrigadierTestHarness(commands);
    }

    /**
     * Recreates the bukkit-generated nodes and applies the brigadier mapping,
     * mirroring what happens on every AsyncPlayerSendCommandsEvent.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    BrigadierTestHarness update() {

        this.labels()
            .filter(label -> this.root.getChild(label) == null)
            .forEach(label -> this.root.addChild(literal(label)
                .then(RequiredArgumentBuilder.<Object, String>argument("args", StringArgumentType.greedyString())
                    .suggests(ASK_SERVER_SUGGESTION))
                .build()));

        this.update(this.data, (RootCommandNode) this.root);
        return this;
    }

    /**
     * Every root-to-leaf path in the tree, rendered as space separated usage
     * text ("quest assign &lt;target&gt;"). Sorted for stable comparison.
     */
    List<String> paths() {
        return this.paths(CommandNode::getChildren);
    }

    /**
     * The same, but walked through the internal literals/arguments indexes.
     * Brigadier parses through those and {@code getChildren().clear()} does not
     * empty them, so a path here that is missing from {@link #paths()} is a
     * node that was supposed to be gone.
     */
    List<String> parsablePaths() {
        return this.paths(BrigadierTestHarness::indexed);
    }

    /**
     * Whether brigadier considers the input a complete, runnable command.
     * The client greys out anything that is not.
     */
    boolean executable(String input) {
        try {
            this.dispatcher().execute(input, new Object());
            return true;
        } catch (CommandSyntaxException exception) {
            return false;
        }
    }

    List<String> suggest(String input) {
        CommandDispatcher<Object> dispatcher = this.dispatcher();
        try {
            return dispatcher.getCompletionSuggestions(dispatcher.parse(input, new Object())).get()
                .getList().stream()
                .map(Suggestion::getText)
                .sorted()
                .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * The same engine the tree was built from, to check the mapping against
     * what the server itself accepts and completes.
     */
    OkaeriCommands commands() {
        return this.commands;
    }

    private CommandDispatcher<Object> dispatcher() {
        return new CommandDispatcher<>(this.root);
    }

    private Stream<String> labels() {
        return this.commands.getRegisteredCommands().stream()
            .map(CommandMeta::getService)
            .flatMap(service -> Stream.concat(Stream.of(service.getLabel()), service.getAliases().stream()))
            .distinct();
    }

    private List<String> paths(Function<CommandNode<Object>, Collection<CommandNode<Object>>> children) {
        List<String> paths = new ArrayList<>();
        for (CommandNode<Object> child : children.apply(this.root)) {
            collect(child, "", children, paths);
        }
        Collections.sort(paths);
        return paths;
    }

    private static void collect(CommandNode<Object> node, String prefix, Function<CommandNode<Object>, Collection<CommandNode<Object>>> children, List<String> out) {

        String name = (node instanceof ArgumentCommandNode) ? ("<" + node.getName() + ">") : node.getName();
        String path = prefix.isEmpty() ? name : (prefix + " " + name);

        Collection<CommandNode<Object>> nodes = children.apply(node);
        if (nodes.isEmpty()) {
            out.add(path);
            return;
        }

        for (CommandNode<Object> child : nodes) {
            collect(child, path, children, out);
        }
    }

    private static Collection<CommandNode<Object>> indexed(CommandNode<Object> node) {
        List<CommandNode<Object>> nodes = new ArrayList<>(index(node, "literals"));
        nodes.addAll(index(node, "arguments"));
        return nodes;
    }

    @SuppressWarnings("unchecked")
    private static Collection<CommandNode<Object>> index(CommandNode<Object> node, String name) {
        try {
            Field field = CommandNode.class.getDeclaredField(name);
            field.setAccessible(true);
            return ((Map<String, CommandNode<Object>>) field.get(node)).values();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read " + name + " of " + node.getName(), exception);
        }
    }
}
