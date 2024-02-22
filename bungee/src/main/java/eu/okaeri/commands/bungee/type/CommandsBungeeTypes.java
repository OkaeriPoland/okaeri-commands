package eu.okaeri.commands.bungee.type;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;

public class CommandsBungeeTypes implements CommandsExtension {

    @Override
    public void register(Commands commands) {
        commands.registerType(new ProxiedPlayerTypeResolver());
    }
}
