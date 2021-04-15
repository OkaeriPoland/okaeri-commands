package eu.okaeri.commands.help;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public abstract class HelpBuilder {

    public abstract String getTemplateForHelp(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String getTemplateForEntry(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String getTemplateForDescription(CommandContext commandContext, InvocationContext invocationContext);

    public abstract String resolveText(CommandContext commandContext, InvocationContext invocationContext, String text);

    public String renderEntry(CommandContext commandContext, InvocationContext invocationContext, CommandMeta meta) {

        ExecutorMeta executor = meta.getExecutor();
        String usage = executor.getUsage()
                .replace("{label}", invocationContext.getLabel())
                .replace("{pattern}", executor.getPattern().getRaw());

        return this.getTemplateForEntry(commandContext, invocationContext)
                .replace("{usage}", usage);
    }

    public String renderDescription(CommandContext commandContext, InvocationContext invocationContext, CommandMeta meta) {
        String description = meta.getExecutor().getDescription();
        String template = this.getTemplateForDescription(commandContext, invocationContext);
        return description.isEmpty() ? "" : template.replace("{description}", this.resolveText(commandContext, invocationContext, description));
    }

    public String render(CommandContext commandContext, InvocationContext invocationContext, CommandsAdapter adapter) {

        String entries = adapter.getCore().getRegistry()
                .findByLabel(invocationContext.getLabel())
                .stream()
                .filter(meta -> meta.getExecutor().getIndex() == 0)
                .map(meta -> {
                    String entry = this.renderEntry(commandContext, invocationContext, meta);
                    String desc = this.renderDescription(commandContext, invocationContext, meta);
                    return entry + (desc.isEmpty() ? "" : "\n" + desc);
                })
                .collect(Collectors.joining("\n"));

        return this.getTemplateForHelp(commandContext, invocationContext)
                .replace("{label}", invocationContext.getLabel())
                .replace("{entries}", entries);
    }
}
