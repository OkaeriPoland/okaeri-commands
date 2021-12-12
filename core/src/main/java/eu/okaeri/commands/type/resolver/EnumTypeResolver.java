package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTypeResolver extends BasicTypeResolver<Enum> {

    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Enum resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {

        Class<? extends Enum> enumType = (Class<? extends Enum>) argumentMeta.getType();

        // 1:1 match ONE=ONE
        try {
            return Enum.valueOf(enumType, text);
        }
        // match first case-insensitive
        catch (Exception ignored) {
            Enum[] enumValues = enumType.getEnumConstants();
            for (Enum value : enumValues) {
                if (!text.equalsIgnoreCase(value.name())) {
                    continue;
                }
                return value;
            }
        }

        // match fail
        String enumValuesStr = Arrays.stream(enumType.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

        throw new IllegalArgumentException("invalid value (available: " + enumValuesStr + ")");
    }
}
