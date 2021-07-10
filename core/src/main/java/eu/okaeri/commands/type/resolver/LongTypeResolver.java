package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class LongTypeResolver extends BasicTypeResolver<Long> {

    @Override
    public boolean supports(Class<?> type) {
        return Long.class.isAssignableFrom(type) || (type == long.class);
    }

    @Override
    public Long resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).longValueExact();
    }
}
