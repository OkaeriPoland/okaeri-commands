package eu.okaeri.commands.type;

import eu.okaeri.commands.type.resolver.TypeResolver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OkaeriCommandsTypes implements CommandsTypes {

    private final List<TypeResolver> typeResolvers = new ArrayList<>();
    private final Map<Type, TypeResolver> resolverCache = new ConcurrentHashMap<>();

    public OkaeriCommandsTypes() {
        this.register(new DefaultCommandsTypesPack());
    }

    @Override
    public void register(CommandsTypesPack typesPack) {
        typesPack.register(this);
        this.resolverCache.clear();
    }

    @Override
    public void register(TypeResolver typeResolver) {
        this.typeResolvers.add(0, typeResolver);
        this.resolverCache.clear();
    }

    @Override
    public void registerExclusive(Type removeAnyForType, TypeResolver typeResolver) {
        this.typeResolvers.removeIf(resolver -> resolver.supports(removeAnyForType));
        this.typeResolvers.add(0, typeResolver);
        this.resolverCache.clear();
    }

    @Override
    public Optional<TypeResolver> findByType(Type type) {

        if (this.resolverCache.containsKey(type)) {
            return Optional.of(this.resolverCache.get(type));
        }

        return this.typeResolvers.stream()
                .filter(resolver -> resolver.supports(type))
                .findFirst()
                .map(resolver -> {
                    this.resolverCache.put(type, resolver);
                    return resolver;
                });
    }
}
