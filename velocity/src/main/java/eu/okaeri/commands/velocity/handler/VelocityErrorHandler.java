package eu.okaeri.commands.velocity.handler;

import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.help.HelpBuilder;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.okaeri.commands.velocity.response.VelocityResponse.MINI_MESSAGE;

public class VelocityErrorHandler implements ErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(VelocityErrorHandler.class.getSimpleName());

    private final ProxyServer proxy;
    private final Commands commands;
    private final HelpBuilder helpBuilder;

    public VelocityErrorHandler(@NonNull ProxyServer proxy, @NonNull Commands commands) {
        this.proxy = proxy;
        this.commands = commands;
        this.helpBuilder = new HelpBuilder() {
            @Override
            public String getTemplateForHelp(CommandData data, Invocation invocation) {
                return VelocityErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageTemplate}",
                    "<gray>Correct usage of /{label}:\n{entries}");
            }

            @Override
            public String getTemplateForEntry(CommandData data, Invocation invocation) {
                return VelocityErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageEntry}",
                    "<reset> - /{usage}");
            }

            @Override
            public String getTemplateForDescription(CommandData data, Invocation invocation) {
                return VelocityErrorHandler.this.resolveText(data, invocation,
                    "${commandSystemUsageEntryDescription}",
                    "<gray>   {description}");
            }

            @Override
            public String resolveText(CommandData data, Invocation invocation, String text) {
                text = commands.resolveText(invocation, data, text); // usage, description
                Component component = MINI_MESSAGE.deserialize(text);
                return PlainTextComponentSerializer.plainText().serialize(component);
            }
        };
    }

    @Override
    @SneakyThrows
    public Object handle(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable throwable) {

        String message = throwable.getMessage();
        if (throwable instanceof NoAccessException) {

            // empty
            if (message.isEmpty()) {
                return MINI_MESSAGE.deserialize(this.resolveText(data, invocation,
                    "${commandSystemAccessError}",
                    "<red>Cannot access command!"
                ));
            }

            // variable
            if (message.startsWith("${") && message.endsWith("}")) {
                return MINI_MESSAGE.deserialize(this.resolveText(data, invocation, message, message));
            }

            // permission
            if (message.matches("[a-zA-Z0-9_\\-\\.]+")) {
                return MINI_MESSAGE.deserialize(this.resolveText(data, invocation,
                    "${commandSystemPermissionsError}",
                    "<red>No permission {permission}!"
                ).replace("{permission}", message));
            }

            // other
            return MINI_MESSAGE.deserialize(this.resolveText(data, invocation,
                "${commandSystemAccessMessageError}",
                "<red>{message}"
            ).replace("{message}", message));
        }

        if (throwable instanceof NoSuchCommandException) {
            return MINI_MESSAGE.deserialize(this.helpBuilder.render(invocation, data, this.commands));
        }

        if (throwable instanceof CommandException) {
            return MINI_MESSAGE.deserialize(this.resolveText(data, invocation,
                "${commandSystemCommandError}",
                "<red>Error: {message}"
            ).replace("{message}", message));
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        LOGGER.log(Level.SEVERE, "Unexpected exception in the command system [id=" + exceptionID + "]", throwable);

        CommandsUnknownErrorEvent event = new CommandsUnknownErrorEvent(data, invocation, throwable, exceptionID, true);
        this.proxy.getEventManager().fire(event).get();

        if (!event.isSendMessage()) {
            return null;
        }

        return MINI_MESSAGE.deserialize(this.resolveText(data, invocation,
            "${commandSystemUnknownError}",
            "<red>Unknown error! Reference ID: {id}"
        ).replace("{id}", exceptionID));
    }

    private String resolveText(CommandData data, Invocation invocation, String key, String def) {
        String text = this.commands.resolveText(invocation, data, key);
        return key.equals(text) ? def : text;
    }
}
