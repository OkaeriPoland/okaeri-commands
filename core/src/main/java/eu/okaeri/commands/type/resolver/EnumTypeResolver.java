package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTypeResolver extends BasicTypeResolver<Enum> {

    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    @SneakyThrows
    public Enum resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {

        // 1:1 match ONE=ONE
        try {
            Method enumMethod = argumentMeta.getType().getMethod("valueOf", String.class);
            Object enumValue = enumMethod.invoke(null, text);
            if (enumValue != null) {
                return (Enum) enumValue;
            }
        }
        // match first case-insensitive
        catch (InvocationTargetException ignored) {
            Enum[] enumValues = (Enum[]) argumentMeta.getType().getEnumConstants();
            for (Enum value : enumValues) {
                if (!text.equalsIgnoreCase(value.name())) {
                    continue;
                }
                return value;
            }
        }

        // match fail
        String enumValuesStr = Arrays.stream(argumentMeta.getType().getEnumConstants())
                .map(item -> ((Enum) item).name())
                .collect(Collectors.joining(", "));

        throw new IllegalArgumentException("invalid value (available: " + enumValuesStr + ")");
    }
}
