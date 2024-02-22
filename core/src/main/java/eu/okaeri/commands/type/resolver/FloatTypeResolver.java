package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.math.BigDecimal;

public class FloatTypeResolver extends BasicTypeResolver<Float> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Float.class.isAssignableFrom(type) || (type == float.class);
    }

    @Override
    public Float resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).floatValue();
    }
}
