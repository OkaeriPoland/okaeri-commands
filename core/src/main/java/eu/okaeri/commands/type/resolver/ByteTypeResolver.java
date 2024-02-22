package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.math.BigDecimal;

public class ByteTypeResolver extends BasicTypeResolver<Byte> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Byte.class.isAssignableFrom(type) || (type == byte.class);
    }

    @Override
    public Byte resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).byteValueExact();
    }
}
