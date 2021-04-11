package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.bukkit.exception.ExceptionSource;
import eu.okaeri.commands.bukkit.exception.NoPermissionException;
import eu.okaeri.commands.bukkit.exception.NoSuchCommandException;
import eu.okaeri.commands.bukkit.response.ErrorResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.help.HelpBuilder;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.InvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;

@SuppressWarnings("FieldNamingConvention")
public class DefaultErrorHandler implements ErrorHandler {

    private static final String K_COMMANDS_SYSTEM_USAGE = "!commands-system-usage";
    private static final String K_COMMANDS_SYSTEM_USAGE_DEF = ChatColor.YELLOW + "Correct usage of /{label}:\n{entries}";
    private static final String K_COMMANDS_SYSTEM_USAGE_ENTRY = "!commands-system-usage-entry";
    private static final String K_COMMANDS_SYSTEM_USAGE_ENTRY_DEF = " - /{usage}";
    private static final String K_COMMANDS_SYSTEM_USAGE_ENTRY_DESCRIPTION = "!commands-system-usage-entry-description";
    private static final String K_COMMANDS_SYSTEM_USAGE_ENTRY_DESCRIPTION_DEF = ChatColor.GRAY + "   {description}";

    private final CommandsAdapter adapter;
    private final HelpBuilder helpBuilder;

    public DefaultErrorHandler(CommandsAdapter adapter) {
        this.adapter = adapter;
        this.helpBuilder = new HelpBuilder() {
            @Override
            public String getTemplateForHelp(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext, K_COMMANDS_SYSTEM_USAGE, K_COMMANDS_SYSTEM_USAGE_DEF);
            }

            @Override
            public String getTemplateForEntry(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext, K_COMMANDS_SYSTEM_USAGE_ENTRY, K_COMMANDS_SYSTEM_USAGE_ENTRY_DEF);
            }

            @Override
            public String getTemplateForDescription(CommandContext commandContext, InvocationContext invocationContext) {
                return DefaultErrorHandler.this.resolveText(commandContext, invocationContext, K_COMMANDS_SYSTEM_USAGE_ENTRY_DESCRIPTION, K_COMMANDS_SYSTEM_USAGE_ENTRY_DESCRIPTION_DEF);
            }
        };
    }

    @Override
    public Object onError(CommandContext commandContext, InvocationContext invocationContext, Throwable throwable, ExceptionSource source) {

        if (throwable instanceof NoPermissionException) {
            return ErrorResponse.of("No permission " + throwable.getMessage() + "!");
        }

        if (throwable instanceof NoSuchCommandException) {
            return RawResponse.of(this.helpBuilder.render(commandContext, invocationContext, this.adapter));
        }

        if (throwable instanceof CommandException) {
            return ErrorResponse.of("Error: " + throwable.getMessage());
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        Bukkit.getLogger().severe("Unexpected exception in the command system [id=" + exceptionID + "]:");
        throwable.printStackTrace();

        return ErrorResponse.of("Unknown error! Reference ID: " + exceptionID);
    }

    private String resolveText(CommandContext commandContext, InvocationContext invocationContext, String key, String def) {
        String text = this.adapter.resolveText(commandContext, invocationContext, key);
        return key.equals(text) ? def : text;
    }
}
