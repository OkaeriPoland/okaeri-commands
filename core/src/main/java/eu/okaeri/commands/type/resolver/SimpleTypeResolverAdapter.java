package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface SimpleTypeResolverAdapter<T> {
    T resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text);
}
