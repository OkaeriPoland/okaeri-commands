package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public class CharSequenceTypeResolver extends BasicTypeResolver<CharSequence> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public CharSequence resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return text;
    }
}
