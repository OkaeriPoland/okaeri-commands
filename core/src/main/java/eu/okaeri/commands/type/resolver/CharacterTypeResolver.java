package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

public class CharacterTypeResolver extends BasicTypeResolver<Character> {

    @Override
    public boolean supports(Class<?> type) {
        return Character.class.isAssignableFrom(type) || (type == char.class);
    }

    @Override
    public Character resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {

        if (text.length() != 1) {
            throw new IllegalArgumentException("non-char provided");
        }

        return text.charAt(0);
    }
}
