package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.handler.ResultHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import org.bukkit.command.CommandSender;

public class DefaultResultHandler implements ResultHandler {

    @Override
    public boolean onResult(Object result, CommandContext commandContext, InvocationContext invocationContext) {

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("cannot return result, no sender found");
        }

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
