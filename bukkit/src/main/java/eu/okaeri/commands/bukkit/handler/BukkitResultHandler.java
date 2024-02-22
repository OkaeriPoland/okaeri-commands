package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

public class BukkitResultHandler implements ResultHandler {

    @Override
    public boolean handle(Object result, @NonNull CommandData data, @NonNull Invocation invocation) {

        CommandSender sender = data.get("sender", CommandSender.class);
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
