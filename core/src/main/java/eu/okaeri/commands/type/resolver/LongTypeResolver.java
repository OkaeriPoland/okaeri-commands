package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

import java.math.BigDecimal;

public class LongTypeResolver extends BasicTypeResolver<Long> {

    @Override
    public boolean supports(Class<?> type) {
        return Long.class.isAssignableFrom(type) || (type == long.class);
    }

    @Override
    public Long resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {
        return new BigDecimal(text).longValueExact();
    }
}
