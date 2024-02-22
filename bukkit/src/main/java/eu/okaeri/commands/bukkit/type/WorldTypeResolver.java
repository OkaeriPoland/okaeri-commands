package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public class WorldTypeResolver extends BasicTypeResolver<World> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return World.class.isAssignableFrom(type);
    }

    @Override
    public World resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        try {
            UUID uniqueId = UUID.fromString(text);
            return Bukkit.getWorld(uniqueId);
        }
        catch (IllegalArgumentException ignored) {
            // case insensitive
            return Bukkit.getWorld(text);
        }
    }
}
