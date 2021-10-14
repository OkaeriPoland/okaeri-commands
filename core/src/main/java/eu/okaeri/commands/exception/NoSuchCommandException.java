package eu.okaeri.commands.exception;

import eu.okaeri.commands.service.CommandException;

public class NoSuchCommandException extends CommandException {

    public NoSuchCommandException(String message) {
        super(message);
    }
}
