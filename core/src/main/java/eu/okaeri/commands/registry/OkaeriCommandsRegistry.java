package eu.okaeri.commands.registry;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import eu.okaeri.commands.service.CommandService;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OkaeriCommandsRegistry implements CommandsRegistry {

    private static final Comparator<CommandMeta> META_COMPARATOR = Comparator
            .comparing((CommandMeta meta) -> {
                List<PatternElement> elements = meta.getExecutor().getPattern().getElements();
                return elements.size();
            }, Comparator.reverseOrder())
            .thenComparing((CommandMeta meta) -> {
                List<PatternElement> elements = meta.getExecutor().getPattern().getElements();
                return elements.stream().filter(element -> element instanceof StaticElement).count();
            }, Comparator.reverseOrder());

    private List<CommandMeta> registeredCommands = new ArrayList<>();
    private final CommandsAdapter adapter;

    public List<CommandMeta> getRegisteredCommands() {
        return Collections.unmodifiableList(this.registeredCommands);
    }

    @Override
    public CommandsRegistry register(Class<? extends CommandService> clazz) {
        return this.register(this.adapter.createInstance(clazz));
    }

    @Override
    public CommandsRegistry register(CommandService service) {

        Class<? extends CommandService> clazz = service.getClass();
        for (Method method : clazz.getDeclaredMethods()) {

            Executor executor = method.getAnnotation(Executor.class);
            if (executor == null) {
                continue;
            }

            List<CommandMeta> commands = CommandMeta.of(service, method);
            for (CommandMeta command : commands) {
                this.registeredCommands.add(command);
                this.adapter.onRegister(command);
            }
        }

        this.registeredCommands.sort(META_COMPARATOR);
        return this;
    }

    @Override
    public List<CommandMeta> findByLabel(String label) {
        return this.registeredCommands.stream()
                .filter(candidate -> candidate.isLabelApplicable(label))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CommandMeta> findByLabelAndArgs(String label, String args) {
        return this.registeredCommands.stream()
                .filter(candidate -> candidate.isLabelApplicable(label))
                .filter(candidate -> candidate.getExecutor().getPattern().matches(args))
                .findFirst();
    }
}
