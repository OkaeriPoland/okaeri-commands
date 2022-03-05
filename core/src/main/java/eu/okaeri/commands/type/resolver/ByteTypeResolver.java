package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class ByteTypeResolver extends BasicTypeResolver<Byte> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Byte.class.isAssignableFrom(type) || (type == byte.class);
    }

    @Override
    public Byte resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).byteValueExact();
    }
}
