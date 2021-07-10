package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public class CharSequenceTypeResolver extends BasicTypeResolver<CharSequence> {

    @Override
    public boolean supports(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public CharSequence resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return text;
    }
}
