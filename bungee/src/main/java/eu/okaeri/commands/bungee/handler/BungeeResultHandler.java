package eu.okaeri.commands.bungee.handler;

import eu.okaeri.commands.bungee.response.BungeeResponse;
import eu.okaeri.commands.bungee.response.RawResponse;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeResultHandler implements ResultHandler {

    @Override
    public boolean handle(Object result, @NonNull CommandData data, @NonNull Invocation invocation) {

        if (result == null) {
            return true;
        }

        CommandSender sender = data.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("cannot return result, no sender found");
        }

        if (result instanceof BaseComponent) {
            sender.sendMessage((BaseComponent) result);
            return true;
        }

        if (result instanceof BaseComponent[]) {
            sender.sendMessage((BaseComponent[]) result);
            return true;
        }

        if (result instanceof BungeeResponse) {
            ((BungeeResponse) result).sendTo(sender);
            return true;
        }

        if (result instanceof CharSequence) {
            RawResponse.of(String.valueOf(result)).sendTo(sender);
            return true;
        }

        return false;
    }
}
