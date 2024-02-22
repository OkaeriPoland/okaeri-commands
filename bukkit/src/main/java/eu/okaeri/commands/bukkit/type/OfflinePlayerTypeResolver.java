package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OfflinePlayerTypeResolver extends BasicTypeResolver<OfflinePlayer> {

    private static final PlayerTypeResolver PLAYER_TYPE_RESOLVER = new PlayerTypeResolver();

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return OfflinePlayer.class.isAssignableFrom(type);
    }

    @Override
    public OfflinePlayer resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        try {
            UUID uniqueId = UUID.fromString(text);
            return Bukkit.getOfflinePlayer(uniqueId);
        }
        catch (IllegalArgumentException ignored) {
            // explicit resolve before getOfflinePlayer gets called
            // who knows what is behind that scary implementation
            // that can make blocking net I/O in Server Thread
            Player onlinePlayer = PLAYER_TYPE_RESOLVER.resolve(invocation, data, argumentMeta, text);
            if (onlinePlayer != null) {
                return onlinePlayer;
            }
            // i'm already scared - warning?
            return Bukkit.getOfflinePlayer(text);
        }
    }
}
