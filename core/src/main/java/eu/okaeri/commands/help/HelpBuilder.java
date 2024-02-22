package eu.okaeri.commands.help;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public abstract class HelpBuilder {

    public abstract String getTemplateForHelp(CommandData data, Invocation invocation);

    public abstract String getTemplateForEntry(CommandData data, Invocation invocation);

    public abstract String getTemplateForDescription(CommandData data, Invocation invocation);

    public abstract String resolveText(CommandData data, Invocation invocation, String text);

    public String renderEntry(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull CommandMeta meta) {

        ExecutorMeta executor = meta.getExecutor();
        String usage = this.resolveText(data, invocation, executor.getUsage())
            .replace("{label}", invocation.getLabel())
            .replace("{pattern}", executor.getPattern().getRaw());

        return this.getTemplateForEntry(data, invocation)
            .replace("{usage}", usage);
    }

    public String renderDescription(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta meta) {
        String description = meta.getExecutor().getDescription();
        String template = this.getTemplateForDescription(data, invocation);
        return description.isEmpty() ? "" : template.replace("{description}", this.resolveText(data, invocation, description));
    }

    public String render(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull Commands commands) {

        String entries = commands
            .findByLabel(invocation.getLabel())
            .stream()
            .filter(meta -> meta.getExecutor().getIndex() == 0)
            .filter(meta -> commands.getAccessHandler().allowAccess(meta.getExecutor(), invocation, data))
            .sorted(Comparator.comparing(meta -> meta.getExecutor().getPattern().getRaw()))
            .collect(Collectors.groupingBy(meta -> meta.getExecutor().getPattern().getRaw().split(" ")[0]))
            .values()
            .stream()
            .sorted(Comparator.<List<CommandMeta>, Integer>comparing(List::size, Comparator.reverseOrder())
                .thenComparing(Comparator.comparing(metas -> metas.stream()
                    .mapToInt(meta -> meta.getExecutor().getPattern().getElements().size())
                    .max()
                    .orElse(0), Comparator.reverseOrder()))
                .thenComparing(Comparator.comparing(metas -> metas.get(0).getExecutor().getPattern().getRaw().split(" ")[0])))
            .flatMap(Collection::stream)
            .map(meta -> {
                String entry = this.renderEntry(data, invocation, meta);
                String desc = this.renderDescription(invocation, data, meta);
                return entry + (desc.isEmpty() ? "" : "\n" + desc);
            })
            .collect(Collectors.joining("\n"));

        return this.getTemplateForHelp(data, invocation)
            .replace("{label}", invocation.getLabel())
            .replace("{entries}", entries);
    }
}
