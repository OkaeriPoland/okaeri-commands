package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerTypeResolver extends BasicTypeResolver<Player> {

    @Override
    public boolean supports(Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Player resolve(InvocationContext invocationContext, CommandContext commandContext, ArgumentMeta argumentMeta, String text) {

        // no player no value
        Player argPlayer = Bukkit.getPlayer(text);
        if (argPlayer == null) {
            return null;
        }

        // if player, make sure that player can see target or is bypassing the check
        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender instanceof Player) {
            Player senderPlayer = (Player) sender;
            if (senderPlayer.canSee(argPlayer) || senderPlayer.hasPermission("okaeri.commands.invisible")) {
                return argPlayer;
            }
            return null;
        }

        // console or other sender
        return argPlayer;
    }
}
