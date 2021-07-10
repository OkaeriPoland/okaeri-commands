package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.math.BigDecimal;

public class FloatTypeResolver extends BasicTypeResolver<Float> {

    @Override
    public boolean supports(Class<?> type) {
        return Float.class.isAssignableFrom(type) || (type == float.class);
    }

    @Override
    public Float resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).floatValue();
    }
}
