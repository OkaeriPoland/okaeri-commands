package eu.okaeri.commands.type;


import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.Optional;

public interface CommandsTypes {

    void register(@NonNull CommandsTypesPack typesPack);

    void register(@NonNull TypeResolver typeResolver);

    void registerExclusive(@NonNull Type removeAnyForType, @NonNull TypeResolver typeResolver);

    Optional<TypeResolver> findByType(@NonNull Type type);
}
