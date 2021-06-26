package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public class CharSequenceTypeResolver extends BasicTypeResolver<CharSequence> {

    @Override
    public boolean supports(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public CharSequence resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {
        return text;
    }
}
