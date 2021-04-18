package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.bukkit.exception.NoPermissionException;
import eu.okaeri.commands.bukkit.exception.NoSuchCommandException;
import eu.okaeri.commands.handler.ErrorHandler;
import eu.okaeri.commands.help.HelpBuilder;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.InvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("FieldNamingConvention")
public class DefaultErrorHandler implements ErrorHandler {

    private final CommandsAdapter adapter;
    private final HelpBuilder helpBuilder;

    public DefaultErrorHandler(CommandsAdapter adapter) {
        this.adapter = adapter;
        this.helpBuilder = new HelpBuilder() {
            @Override
            public String getTemplateForHelp(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext,
                        "!commands-system-usage-template",
                        ChatColor.YELLOW + "Correct usage of /{label}:\n{entries}");
            }

            @Override
            public String getTemplateForEntry(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext,
                        "!commands-system-usage-entry",
                        ChatColor.RESET + " - /{usage}");
            }

            @Override
            public String getTemplateForDescription(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext,
                        "!commands-system-usage-entry-description",
                        ChatColor.GRAY + "   {description}");
            }

            @Override
            public String resolveText(CommandContext commandContext, InvocationContext invocationContext, String text) {
                return ChatColor.stripColor(adapter.resolveText(commandContext, invocationContext, text)); // usage, description
            }
        };
    }

    @Override
    public Object onError(CommandContext commandContext, InvocationContext invocationContext, Throwable throwable) {

        if (throwable instanceof NoPermissionException) {
            return this.resolveText(commandContext, invocationContext,
                    "!comamnds-system-permissions-error", ChatColor.RED + "No permission {permission}!")
                    .replace("{permission}", throwable.getMessage());
        }

        if (throwable instanceof NoSuchCommandException) {
            return this.helpBuilder.render(commandContext, invocationContext, this.adapter);
        }

        if (throwable instanceof CommandException) {
            return this.resolveText(commandContext, invocationContext,
                    "!comamnds-system-command-error", ChatColor.RED + "Error: {message}")
                    .replace("{message}", throwable.getMessage());
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        Bukkit.getLogger().log(Level.SEVERE, "Unexpected exception in the command system [id=" + exceptionID + "]", throwable);

        CommandsUnknownErrorEvent event = new CommandsUnknownErrorEvent(!Bukkit.isPrimaryThread(), commandContext, invocationContext, throwable, exceptionID, true);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isSendMessage()) {
            return null;
        }

        return this.resolveText(commandContext, invocationContext,
                "!commands-system-unknown-error", ChatColor.RED + "Unknown error! Reference ID: {id}")
                .replace("{id}", exceptionID);
    }

    private String resolveText(CommandContext commandContext, InvocationContext invocationContext, String key, String def) {
        String text = this.adapter.resolveText(commandContext, invocationContext, key);
        return key.equals(text) ? def : text;
    }
}
