package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.exception.ExceptionSource;
import eu.okaeri.commands.bukkit.exception.NoPermissionException;
import eu.okaeri.commands.bukkit.exception.NoSuchCommandException;
import eu.okaeri.commands.bukkit.response.ErrorResponse;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandException;
import org.bukkit.Bukkit;

import java.util.UUID;

public class DefaultErrorHandler implements ErrorHandler {

    @Override
    public Object onError(CommandContext context, Throwable throwable, ExceptionSource source) {

        if (throwable instanceof NoPermissionException) {
            return ErrorResponse.of("No permission " + throwable.getMessage() + "!");
        }

        if (throwable instanceof NoSuchCommandException) {
            return ErrorResponse.of("No such command!");
        }

        if (throwable instanceof CommandException) {
            return ErrorResponse.of("Error: " + throwable.getMessage());
        }

        String exceptionID = String.valueOf(UUID.randomUUID()).split("-")[4];
        Bukkit.getLogger().severe("Unexpected exception in the command system [id=" + exceptionID + "]:");
        throwable.printStackTrace();

        return ErrorResponse.of("Unknown error! Reference ID: " + exceptionID);
    }
}
