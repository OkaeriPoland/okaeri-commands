package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class OfflinePlayerTypeResolver extends BasicTypeResolver<OfflinePlayer> {

    @Override
    public boolean supports(Class<?> type) {
        return OfflinePlayer.class.isAssignableFrom(type);
    }

    @Override
    public OfflinePlayer resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {
        try {
            UUID uniqueId = UUID.fromString(text);
            return Bukkit.getOfflinePlayer(uniqueId);
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getOfflinePlayer(text);
        }
    }
}
