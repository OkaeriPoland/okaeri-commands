package eu.okaeri.commands.velocity.type;

import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsVelocityTypes implements CommandsExtension {

    private final ProxyServer proxy;

    @Override
    public void register(Commands commands) {
        commands.registerType(new PlayerTypeResolver(this.proxy));
    }
}
