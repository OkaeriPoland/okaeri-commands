package eu.okaeri.commands.exception;

import eu.okaeri.commands.service.CommandException;
import lombok.Getter;

@Getter
public class InvalidContextException extends CommandException {

    private final Class<?> expectedType;
    private final Class<?> actualType;

    public InvalidContextException(String message, Class<?> expectedType, Class<?> actualType) {
        super(message);
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public InvalidContextException(Class<?> expectedType, Class<?> actualType) {
        super("");
        this.expectedType = expectedType;
        this.actualType = actualType;
    }
}
