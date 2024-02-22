package eu.okaeri.commands.handler.validation;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.lang.reflect.Parameter;

public class DefaultParameterValidationHandler implements ParameterValidationHandler {

    @Override
    public ValidationResult validate(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, Object value, int index) {
        return ValidationResult.ok("no validation was performed");
    }
}
