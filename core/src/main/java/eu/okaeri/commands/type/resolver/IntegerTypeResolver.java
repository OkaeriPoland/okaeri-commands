package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

import java.math.BigDecimal;

public class IntegerTypeResolver extends BasicTypeResolver<Integer> {

    @Override
    public boolean supports(Class<?> type) {
        return Integer.class.isAssignableFrom(type) || (type == int.class);
    }

    @Override
    public Integer resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {
        return new BigDecimal(text).intValueExact();
    }
}
