package eu.okaeri.commands.handler.validation;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.lang.reflect.Parameter;

public interface ParameterValidationHandler {
    ValidationResult validate(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, Object value, int index);
}
