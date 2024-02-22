package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.math.BigDecimal;

public class LongTypeResolver extends BasicTypeResolver<Long> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Long.class.isAssignableFrom(type) || (type == long.class);
    }

    @Override
    public Long resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).longValueExact();
    }
}
