package eu.okaeri.commands.type.resolver;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleTypeResolver<T> extends BasicTypeResolver<T> {

    private final Class<T> type;
    private final SimpleTypeResolverAdapter<T> adapter;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    @Override
    public T resolve(@NonNull Invocation inv, @NonNull CommandData com, @NonNull ArgumentMeta arg, @NonNull String text) {
        return this.adapter.resolve(inv, com, arg, text);
    }
}
