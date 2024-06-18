package eu.okaeri.commands.velocity.type;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerTypeResolver extends BasicTypeResolver<Player> {

    private final ProxyServer proxyServer;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Player resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return this.proxyServer.getPlayer(text).orElse(null);
    }
}
