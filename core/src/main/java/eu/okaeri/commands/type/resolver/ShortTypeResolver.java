package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class ShortTypeResolver extends BasicTypeResolver<Short> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Short.class.isAssignableFrom(type) || (type == short.class);
    }

    @Override
    public Short resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).shortValueExact();
    }
}
