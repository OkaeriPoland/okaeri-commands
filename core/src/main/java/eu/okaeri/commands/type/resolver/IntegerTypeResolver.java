package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.math.BigDecimal;

public class IntegerTypeResolver extends BasicTypeResolver<Integer> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Integer.class.isAssignableFrom(type) || (type == int.class);
    }

    @Override
    public Integer resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return new BigDecimal(text).intValueExact();
    }
}
