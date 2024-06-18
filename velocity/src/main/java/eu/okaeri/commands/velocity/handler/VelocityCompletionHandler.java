package eu.okaeri.commands.velocity.handler;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
public class VelocityCompletionHandler extends DefaultCompletionHandler {

    private final ProxyServer proxy;

    @Override
    public void registerNamed(@NonNull Commands commands) {
        super.registerNamed(commands);
        commands.registerCompletion("velocity:player:name", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, Player::getUsername));
        commands.registerCompletion("velocity:player:address", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getRemoteAddress().getAddress().getHostAddress()));
        commands.registerCompletion("velocity:player:uuid", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getUniqueId().toString()));
    }

    protected List<String> completePlayer(@NotNull ArgumentMeta argument, @NotNull Invocation invocation, @NonNull CommandData data, @NonNull Function<Player, String> mapper) {

        int limit = this.getLimit(argument, invocation);
        CommandSource sender = data.get("sender", CommandSource.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;

        boolean completeSelf = this.getData(argument, invocation,
            "self", () -> true,
            Boolean::parseBoolean
        );

        return this.filter(limit, this.stringFilter(invocation), this.proxy.getAllPlayers().stream()
            .filter(onlinePlayer -> {
                // non-player senders complete all players
                if (player == null) {
                    return true;
                }
                // sender should be hidden when self=false
                if (!completeSelf && onlinePlayer.equals(player)) {
                    return false;
                }
                // complete otherwise
                return true;
            })
            .map(mapper));
    }

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull Invocation invocation, @NonNull CommandData data) {

        Class<?> type = argument.getType();
        int limit = this.getLimit(argument, invocation);

        if (Player.class.isAssignableFrom(type)) {
            return this.completePlayer(argument, invocation, data, Player::getUsername);
        }

        return super.complete(argument, invocation, data);
    }
}
