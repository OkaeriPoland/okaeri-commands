package eu.okaeri.commands.bungee.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
public class BungeeCompletionHandler extends DefaultCompletionHandler {

    @Override
    public void registerNamed(@NonNull Commands commands) {
        super.registerNamed(commands);
        commands.registerCompletion("bukkit:player:name", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, ProxiedPlayer::getName));
        commands.registerCompletion("bukkit:player:address", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getAddress().getAddress().getHostAddress()));
        commands.registerCompletion("bukkit:player:uuid", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getUniqueId().toString()));
    }

    protected List<String> completePlayer(@NotNull ArgumentMeta argument, @NotNull Invocation invocation, @NonNull CommandData data, @NonNull Function<ProxiedPlayer, String> mapper) {

        int limit = this.getLimit(argument, invocation);
        CommandSender sender = data.get("sender", CommandSender.class);
        ProxiedPlayer player = (sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender) : null;

        boolean completeSelf = this.getData(argument, invocation,
            "self", () -> true,
            Boolean::parseBoolean
        );

        return this.filter(limit, this.stringFilter(invocation), ProxyServer.getInstance().getPlayers().stream()
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

        if (ProxiedPlayer.class.isAssignableFrom(type)) {
            return this.completePlayer(argument, invocation, data, ProxiedPlayer::getName);
        }

        return super.complete(argument, invocation, data);
    }
}
