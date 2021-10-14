package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.type.CommandsTypes;
import eu.okaeri.commands.type.CommandsTypesPack;

public class CommandsBukkitTypes implements CommandsTypesPack {

    @Override
    public void register(CommandsTypes types) {
        types.register(new OfflinePlayerTypeResolver());
        types.register(new PlayerTypeResolver()); // always after OfflinePlayer, order matters
        types.register(new WorldTypeResolver());
    }
}
