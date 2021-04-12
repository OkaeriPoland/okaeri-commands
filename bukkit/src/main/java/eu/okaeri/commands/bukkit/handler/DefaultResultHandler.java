package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import org.bukkit.command.CommandSender;

public class DefaultResultHandler implements ResultHandler {

    @Override
    public boolean onResult(Object result, CommandSender sender) {

        if (result instanceof BukkitResponse) {
            ((BukkitResponse) result).sendTo(sender);
            return true;
        }

        if (result instanceof CharSequence) {
            RawResponse.of(String.valueOf(result)).sendTo(sender);
            return true;
        }

        return false;
    }
}
