package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;

public class CommandsBukkitTypes implements CommandsExtension {

    @Override
    public void register(Commands commands) {
        commands.registerType(new OfflinePlayerTypeResolver());
        commands.registerType(new PlayerTypeResolver()); // always after OfflinePlayer, order matters
        commands.registerType(new EnchantmentTypeResolver());
        commands.registerType(new PotionEffectTypeResolver());
        commands.registerType(new WorldTypeResolver());
    }
}
