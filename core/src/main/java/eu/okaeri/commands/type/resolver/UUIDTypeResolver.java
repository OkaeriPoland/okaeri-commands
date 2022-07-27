package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

import java.util.UUID;

public class UUIDTypeResolver extends BasicTypeResolver<UUID> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return UUID.class.isAssignableFrom(type);
    }

    @Override
    public UUID resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return UUID.fromString(text);
    }
}
