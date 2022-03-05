package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class DoubleTypeResolver extends BasicTypeResolver<Double> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Double.class.isAssignableFrom(type) || (type == double.class);
    }

    @Override
    public Double resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).doubleValue();
    }
}
