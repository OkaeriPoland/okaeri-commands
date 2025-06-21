package eu.okaeri.commands.validator;

import eu.okaeri.commands.handler.validation.DefaultParameterValidationHandler;
import eu.okaeri.commands.handler.validation.ValidationResult;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.service.Option;
import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.Validator;
import eu.okaeri.validator.provider.ValidationProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OkaeriParameterValidationHandler extends DefaultParameterValidationHandler {

    @NonNull
    private final Validator validator;

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, Object value, int index) {

        Set<ConstraintViolation> constraintViolations = this.validator.getRegisteredProviders().values().stream()
            .filter(provider -> provider.shouldValidate(param))
            .map(provider -> (ValidationProvider) provider)
            .flatMap(provider -> {
                Object unwrappedValue = this.unwrapValue(value);
                Type unwrappedType = this.unwrapType(param);
                Set<ConstraintViolation> violations = provider.validate(
                    param.getAnnotation(provider.getAnnotation()),
                    param,
                    unwrappedValue,
                    ((Class<?>) unwrappedType),
                    unwrappedType,
                    param.getName());
                return violations.stream();
            })
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (constraintViolations.isEmpty()) {
            return super.validate(invocation, data, command, param, value, index);
        }

        return ValidationResult.invalid(constraintViolations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", ")));
    }

    private Type unwrapType(@NonNull Parameter parameter) {
        Type type = parameter.getParameterizedType();
        if (!(type instanceof ParameterizedType)) {
            return type;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class)) {
            throw new IllegalArgumentException("Validation of complex types is not supported: " + parameter);
        }
        if (Option.class.isAssignableFrom((Class<?>) rawType)) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        if (Optional.class.isAssignableFrom((Class<?>) rawType)) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return type;
    }

    @SuppressWarnings("unchecked")
    private Object unwrapValue(Object value) {
        if (value instanceof Option) {
            return ((Option) value).orElse(null);
        }
        if (value instanceof Optional) {
            return ((Optional) value).orElse(null);
        }
        return value;
    }
}
