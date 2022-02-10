package eu.okaeri.commands.handler.validation;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.lang.reflect.Parameter;

public class DefaultParameterValidationHandler implements ParameterValidationHandler {

    @Override
    public ValidationResult validate(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta command, @NonNull Parameter param, Object value, int index) {
        return ValidationResult.ok("no validation was performed");
    }
}
