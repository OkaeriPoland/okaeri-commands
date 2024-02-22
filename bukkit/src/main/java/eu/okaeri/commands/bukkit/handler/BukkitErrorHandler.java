package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.help.HelpBuilder;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.Invocation;
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
            public String getTemplateForHelp(CommandData data, Invocation invocation) {
                return BukkitErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageTemplate}",
                    ChatColor.YELLOW + "Correct usage of /{label}:\n{entries}");
            }

            @Override
            public String getTemplateForEntry(CommandData data, Invocation invocation) {
                return BukkitErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageEntry}",
                    ChatColor.RESET + " - /{usage}");
            }

            @Override
            public String getTemplateForDescription(CommandData data, Invocation invocation) {
                return BukkitErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageEntryDescription}",
                    ChatColor.GRAY + "   {description}");
            }

            @Override
            public String resolveText(CommandData data, Invocation invocation, String text) {
                return ChatColor.stripColor(commands.resolveText(invocation, data, text)); // usage, description
            }
        };
    }

    @Override
    public Object handle(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable throwable) {

        String message = throwable.getMessage();
        if (throwable instanceof NoAccessException) {

            // empty
            if (message.isEmpty()) {
                return this.resolveText(data, invocation,
                    "${commandSystemAccessError}",
                    ChatColor.RED + "Cannot access command!"
                );
            }

            // variable
            if (message.startsWith("${") && message.endsWith("}")) {
                return this.resolveText(data, invocation, message, message);
            }

            // permission
            if (message.matches("[a-zA-Z0-9_\\-\\.]+")) {
                return this.resolveText(data, invocation,
                    "${commandSystemPermissionsError}",
                    ChatColor.RED + "No permission {permission}!"
                ).replace("{permission}", message);
            }

            // other
            return this.resolveText(data, invocation,
                "${commandSystemAccessMessageError}",
                ChatColor.RED + "{message}"
            ).replace("{message}", message);
        }

        if (throwable instanceof NoSuchCommandException) {
            return this.helpBuilder.render(invocation, data, this.commands);
        }

        if (throwable instanceof CommandException) {
            return this.resolveText(data, invocation,
                "${commandSystemCommandError}",
                ChatColor.RED + "Error: {message}"
            ).replace("{message}", message);
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        Bukkit.getLogger().log(Level.SEVERE, "Unexpected exception in the command system [id=" + exceptionID + "]", throwable);

        CommandsUnknownErrorEvent event = new CommandsUnknownErrorEvent(!Bukkit.isPrimaryThread(), data, invocation, throwable, exceptionID, true);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isSendMessage()) {
            return null;
        }

        return this.resolveText(data, invocation,
            "${commandSystemUnknownError}",
            ChatColor.RED + "Unknown error! Reference ID: {id}"
        ).replace("{id}", exceptionID);
    }

    private String resolveText(CommandData data, Invocation invocation, String key, String def) {
        String text = this.commands.resolveText(invocation, data, key);
        return key.equals(text) ? def : text;
    }
}
