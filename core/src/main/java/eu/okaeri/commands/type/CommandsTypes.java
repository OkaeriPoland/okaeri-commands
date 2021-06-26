package eu.okaeri.commands.type;


import eu.okaeri.commands.type.resolver.TypeResolver;

import java.lang.reflect.Type;
import java.util.Optional;

public interface CommandsTypes {

    void register(CommandsTypesPack typesPack);

    void register(TypeResolver typeResolver);

    void registerExclusive(Type removeAnyForType, TypeResolver typeResolver);

    Optional<TypeResolver> findByType(Type type);
}
