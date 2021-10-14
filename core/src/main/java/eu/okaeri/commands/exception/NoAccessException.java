package eu.okaeri.commands.exception;

import eu.okaeri.commands.service.CommandException;

public class NoAccessException extends CommandException {

    public NoAccessException(String message) {
        super(message);
    }
}
