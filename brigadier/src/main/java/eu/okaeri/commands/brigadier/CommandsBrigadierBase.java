package eu.okaeri.commands.brigadier;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;


public class CommandsBrigadierBase {

    protected final Map<Class<?>, ArgumentType<?>> argumentTypes = new HashMap<>();
    protected final Set<Class<?>> staticTypes = new HashSet<>();
    protected OkaeriCommands commands;
    protected Set<String> allLabels;

    public CommandsBrigadierBase() {
        // register argument types
        this.argumentTypes.put(boolean.class, BoolArgumentType.bool());
        this.argumentTypes.put(Boolean.class, BoolArgumentType.bool());
        this.argumentTypes.put(double.class, DoubleArgumentType.doubleArg());
        this.argumentTypes.put(Double.class, DoubleArgumentType.doubleArg());
        this.argumentTypes.put(float.class, FloatArgumentType.floatArg());
        this.argumentTypes.put(Float.class, FloatArgumentType.floatArg());
        this.argumentTypes.put(int.class, IntegerArgumentType.integer());
        this.argumentTypes.put(Integer.class, IntegerArgumentType.integer());
        this.argumentTypes.put(long.class, LongArgumentType.longArg());
        this.argumentTypes.put(Long.class, LongArgumentType.longArg());
        // register static types
        this.staticTypes.add(boolean.class);
        this.staticTypes.add(Boolean.class);
    }

    @SuppressWarnings("unchecked")
    public void update(CommandContext commandContext, RootCommandNode commandNode) {

        // delay dumping all labels to first player
        if (this.allLabels == null) {
            this.allLabels = this.commands.getRegisteredCommands().stream()
                .map(CommandMeta::getService)
                .flatMap(service -> Stream.concat(Stream.of(service.getLabel()), service.getAliases().stream()))
                .collect(Collectors.toSet());
        }

        // update all known commands
        for (String label : this.allLabels) {

            // check if node exists for this label
            // and has autogenerated greedy args
            CommandNode oldNode = commandNode.getChild(label);
            CommandNode argsChild = oldNode.getChild("args");
            if (argsChild == null) {
                continue;
            }

            // extract ask_server suggestion
            // will come in handy later
            ArgumentCommandNode args = (ArgumentCommandNode) argsChild;
            SuggestionProvider askServerSuggestion = args.getCustomSuggestions();

            // get metas for label
            List<CommandMeta> metas = this.commands.findByLabel(label);
            if (metas.isEmpty()) {
                continue;
            }

            // check access (permissions)
            ServiceMeta service = metas.get(0).getService();
            InvocationContext dummyContext = InvocationContext.of(service, service.getLabel(), new String[0]);
            if (!this.commands.getAccessHandler().allowAccess(service, dummyContext, commandContext, false)) {
                continue;
            }

            // clear current node
            oldNode.getChildren().clear();
            for (CommandMeta meta : metas) {

                // we may need that later
                ExecutorMeta executor = meta.getExecutor();
                PatternMeta pattern = executor.getPattern();

                // check access
                InvocationContext invocationContext = InvocationContext.of(meta, service.getLabel(), "");
                if (!this.commands.getAccessHandler().allowAccess(executor, invocationContext, commandContext)) {
                    continue;
                }

                // track tree position
                List<CommandNode> currentNodes = new ArrayList<>(Collections.singletonList(oldNode));

                // transform pattern elements into nodes
                for (PatternElement patternElement : pattern.getElements()) {

                    // static elements are easy, just use literal
                    if (patternElement instanceof StaticElement) {
                        LiteralCommandNode<Object> literal = literal(patternElement.getName()).build();
                        currentNodes.forEach(node -> node.addChild(literal));
                        currentNodes.clear();
                        currentNodes.add(literal);
                        continue;
                    }

                    // we need to know the type
                    ArgumentMeta argumentMeta = pattern.getArgumentByName(patternElement.getName()).orElse(null);

                    // check for custom completions
                    List<String> customCompletions = executor.getCompletion().getCompletions(patternElement.getName());
                    if (!customCompletions.isEmpty()) {
                        // this section bifurcates so we need to track it separately
                        List<CommandNode> newNodes = new ArrayList<>();
                        // every completion needs to be added separately
                        for (String completion : customCompletions) {
                            // dynamic completion
                            if (completion.startsWith("@")) {
                                ArgumentType type = this.resolveType(argumentMeta, patternElement);
                                ArgumentCommandNode argument = argument(patternElement.getName(), type).suggests(askServerSuggestion).build();
                                currentNodes.forEach(node -> node.addChild(argument));
                                newNodes.add(argument);
                            }
                            // static completion
                            else {
                                LiteralCommandNode<Object> literal = literal(completion).build();
                                currentNodes.forEach(node -> node.addChild(literal));
                                newNodes.add(literal);
                            }
                        }
                        // update current nodes with new list
                        currentNodes.clear();
                        currentNodes.addAll(newNodes);
                        continue;
                    }

                    // try generating static completions
                    if (argumentMeta != null) {

                        // get already resolved type
                        Class<?> type = argumentMeta.getType();

                        // only certain types can be assumed as static
                        if (this.canAssumeStatic(type)) {
                            // resolve completions using completion handler
                            List<String> argumentCompletions = this.commands.getCompletionHandler().complete(argumentMeta, invocationContext, commandContext);
                            // this section bifurcates so we need to track it separately
                            List<CommandNode> newNodes = new ArrayList<>();
                            // every completion needs to be added separately
                            for (String completion : argumentCompletions) {
                                LiteralCommandNode<Object> literal = literal(completion).build();
                                currentNodes.forEach(node -> node.addChild(literal));
                                newNodes.add(literal);
                            }
                            // update current nodes with new list
                            currentNodes.clear();
                            currentNodes.addAll(newNodes);
                            continue;
                        }
                    }

                    // dynamic elements may be complicated, use ask_server
                    ArgumentType type = this.resolveType(argumentMeta, patternElement);
                    ArgumentCommandNode argument = argument(patternElement.getName(), type).suggests(askServerSuggestion).build();
                    currentNodes.forEach(node -> node.addChild(argument));
                    currentNodes.clear();
                    currentNodes.add(argument);
                }
            }
        }
    }

    protected ArgumentType resolveType(ArgumentMeta argument, PatternElement patternElement) {

        // get argument type from map
        if ((argument != null)) {
            ArgumentType<?> argumentType = this.argumentTypes.get(argument.getType());
            if (argumentType != null) {
                return argumentType;
            }
        }

        // fallback and support for greedy strings
        return (patternElement.getWidth() == -1)
            ? StringArgumentType.greedyString()
            : StringArgumentType.word();
    }

    protected boolean canAssumeStatic(Class<?> type) {
        return type.isEnum() || this.staticTypes.contains(type);
    }
}
