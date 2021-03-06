package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.help.HelpBuilder;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("FieldNamingConvention")
public class BukkitErrorHandler implements ErrorHandler {

    private final Commands commands;
    private final HelpBuilder helpBuilder;

    public BukkitErrorHandler(@NonNull Commands commands) {
        this.commands = commands;
        this.helpBuilder = new HelpBuilder() {
            @Override
            public String getTemplateForHelp(CommandContext commandContext, InvocationContext invocationContext) {
                return BukkitErrorHandler.this.resolveText(commandContext, invocationContext,
                    "${commandSystemUsageTemplate}",
                    ChatColor.YELLOW + "Correct usage of /{label}:\n{entries}");
            }

            @Override
            public String getTemplateForEntry(CommandContext commandContext, InvocationContext invocationContext) {
                return BukkitErrorHandler.this.resolveText(commandContext, invocationContext,
                    "${commandSystemUsageEntry}",
                    ChatColor.RESET + " - /{usage}");
            }

            @Override
            public String getTemplateForDescription(CommandContext commandContext, InvocationContext invocationContext) {
                return BukkitErrorHandler.this.resolveText(commandContext, invocationContext,
                    "${commandSystemUsageEntryDescription}",
                    ChatColor.GRAY + "   {description}");
            }

            @Override
            public String resolveText(CommandContext commandContext, InvocationContext invocationContext, String text) {
                return ChatColor.stripColor(commands.resolveText(invocationContext, commandContext, text)); // usage, description
            }
        };
    }

    @Override
    public Object handle(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable throwable) {

        String message = throwable.getMessage();
        if (throwable instanceof NoAccessException) {

            // empty
            if (message.isEmpty()) {
                return this.resolveText(commandContext, invocationContext,
                    "${commandSystemAccessError}",
                    ChatColor.RED + "Cannot access command!"
                );
            }

            // variable
            if (message.startsWith("${") && message.endsWith("}")) {
                return this.resolveText(commandContext, invocationContext, message, message);
            }

            // permission
            if (message.matches("[a-zA-Z0-9_\\-\\.]+")) {
                return this.resolveText(commandContext, invocationContext,
                    "${commandSystemPermissionsError}",
                    ChatColor.RED + "No permission {permission}!"
                ).replace("{permission}", message);
            }

            // other
            return this.resolveText(commandContext, invocationContext,
                "${commandSystemAccessMessageError}",
                ChatColor.RED + "{message}"
            ).replace("{message}", message);
        }

        if (throwable instanceof NoSuchCommandException) {
            return this.helpBuilder.render(invocationContext, commandContext, this.commands);
        }

        if (throwable instanceof CommandException) {
            return this.resolveText(commandContext, invocationContext,
                "${commandSystemCommandError}",
                ChatColor.RED + "Error: {message}"
            ).replace("{message}", message);
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        Bukkit.getLogger().log(Level.SEVERE, "Unexpected exception in the command system [id=" + exceptionID + "]", throwable);

        CommandsUnknownErrorEvent event = new CommandsUnknownErrorEvent(!Bukkit.isPrimaryThread(), commandContext, invocationContext, throwable, exceptionID, true);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isSendMessage()) {
            return null;
        }

        return this.resolveText(commandContext, invocationContext,
            "${commandSystemUnknownError}",
            ChatColor.RED + "Unknown error! Reference ID: {id}"
        ).replace("{id}", exceptionID);
    }

    private String resolveText(CommandContext commandContext, InvocationContext invocationContext, String key, String def) {
        String text = this.commands.resolveText(invocationContext, commandContext, key);
        return key.equals(text) ? def : text;
    }
}
