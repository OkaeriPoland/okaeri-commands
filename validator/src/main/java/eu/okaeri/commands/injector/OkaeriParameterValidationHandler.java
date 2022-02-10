package eu.okaeri.commands.injector;

import eu.okaeri.commands.handler.validation.DefaultParameterValidationHandler;
import eu.okaeri.commands.handler.validation.ValidationResult;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OkaeriParameterValidationHandler extends DefaultParameterValidationHandler {

    @NonNull
    private final Validator validator;

    @Override
    public ValidationResult validate(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta command, @NonNull Parameter param, Object value, int index) {

        Set<ConstraintViolation> constraintViolations = this.validator.validateParameter(param, value);
        if (constraintViolations.isEmpty()) {
            return super.validate(invocationContext, commandContext, command, param, value, index);
        }

        return ValidationResult.invalid(constraintViolations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", ")));
    }
}
