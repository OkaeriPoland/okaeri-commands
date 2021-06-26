package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;

import java.lang.reflect.Type;

public interface TypeResolver<T> {

    boolean supports(Type type);

    boolean supports(Class<?> type);

    T resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text);
}
