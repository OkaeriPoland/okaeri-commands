package eu.okaeri.commands.velocity.handler;

import com.velocitypowered.api.command.CommandSource;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.velocity.response.RawResponse;
import eu.okaeri.commands.velocity.response.VelocityResponse;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public class VelocityResultHandler implements ResultHandler {

    @Override
    public boolean handle(Object result, @NonNull CommandData data, @NonNull Invocation invocation) {

        if (result == null) {
            return true;
        }

        CommandSource sender = data.get("sender", CommandSource.class);
        if (sender == null) {
            throw new RuntimeException("cannot return result, no sender found");
        }

        if (result instanceof Component) {
            sender.sendMessage((ComponentLike) result);
            return true;
        }

        if (result instanceof VelocityResponse) {
            ((VelocityResponse) result).sendTo(sender);
            return true;
        }

        if (result instanceof CharSequence) {
            RawResponse.of(String.valueOf(result)).sendTo(sender);
            return true;
        }

        return false;
    }
}
