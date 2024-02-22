package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.util.UUID;

public class UUIDTypeResolver extends BasicTypeResolver<UUID> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return UUID.class.isAssignableFrom(type);
    }

    @Override
    public UUID resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return UUID.fromString(text);
    }
}
