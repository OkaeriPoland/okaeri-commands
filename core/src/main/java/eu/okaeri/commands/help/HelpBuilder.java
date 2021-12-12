package eu.okaeri.commands.help;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public abstract class HelpBuilder {

    public abstract String getTemplateForHelp(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String getTemplateForEntry(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String getTemplateForDescription(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String resolveText(CommandContext commandContext, InvocationContext invocationContext, String text);

    public String renderEntry(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull CommandMeta meta) {

        ExecutorMeta executor = meta.getExecutor();
        String usage = this.resolveText(commandContext, invocationContext, executor.getUsage())
            .replace("{label}", invocationContext.getLabel())
            .replace("{pattern}", executor.getPattern().getRaw());

        return this.getTemplateForEntry(commandContext, invocationContext)
            .replace("{usage}", usage);
    }

    public String renderDescription(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta meta) {
        String description = meta.getExecutor().getDescription();
        String template = this.getTemplateForDescription(commandContext, invocationContext);
        return description.isEmpty() ? "" : template.replace("{description}", this.resolveText(commandContext, invocationContext, description));
    }

    public String render(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull Commands commands) {

        String entries = commands
            .findByLabel(invocationContext.getLabel())
            .stream()
            .filter(meta -> meta.getExecutor().getIndex() == 0)
            .filter(meta -> commands.getAccessHandler().allowAccess(meta.getExecutor(), invocationContext, commandContext))
            .map(meta -> {
                String entry = this.renderEntry(commandContext, invocationContext, meta);
                String desc = this.renderDescription(invocationContext, commandContext, meta);
                return entry + (desc.isEmpty() ? "" : "\n" + desc);
            })
            .collect(Collectors.joining("\n"));

        return this.getTemplateForHelp(commandContext, invocationContext)
            .replace("{label}", invocationContext.getLabel())
            .replace("{entries}", entries);
    }
}
