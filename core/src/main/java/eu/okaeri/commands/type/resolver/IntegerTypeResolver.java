package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class IntegerTypeResolver extends BasicTypeResolver<Integer> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Integer.class.isAssignableFrom(type) || (type == int.class);
    }

    @Override
    public Integer resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).intValueExact();
    }
}
