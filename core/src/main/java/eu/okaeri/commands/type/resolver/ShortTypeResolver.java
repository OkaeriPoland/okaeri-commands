package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

import java.math.BigDecimal;

public class ShortTypeResolver extends BasicTypeResolver<Short> {

    @Override
    public boolean supports(Class<?> type) {
        return Short.class.isAssignableFrom(type) || (type == short.class);
    }

    @Override
    public Short resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {
        return new BigDecimal(text).shortValueExact();
    }
}
