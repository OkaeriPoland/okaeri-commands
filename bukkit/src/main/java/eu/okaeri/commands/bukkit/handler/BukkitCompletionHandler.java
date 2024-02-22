package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
public class BukkitCompletionHandler extends DefaultCompletionHandler {

    @Override
    public void registerNamed(@NonNull Commands commands) {
        super.registerNamed(commands);
        commands.registerCompletion("bukkit:player:name", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, HumanEntity::getName));
        commands.registerCompletion("bukkit:player:address", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getAddress().getAddress().getHostAddress()));
        commands.registerCompletion("bukkit:player:uuid", (completion, argument, invocation, data) ->
            this.completePlayer(argument, invocation, data, player -> player.getUniqueId().toString()));
        commands.registerCompletion("bukkit:enchantment", (completion, argument, invocation, data) ->
            this.completeEnchantment(invocation, this.getLimit(argument, invocation)));
        commands.registerCompletion("bukkit:potioneffecttype", (completion, argument, invocation, data) ->
            this.completePotionEffectType(invocation, this.getLimit(argument, invocation)));
        commands.registerCompletion("bukkit:world", (completion, argument, invocation, data) ->
            this.completeWorld(invocation, this.getLimit(argument, invocation)));
    }

    protected List<String> completePlayer(@NotNull ArgumentMeta argument, @NotNull Invocation invocation, @NonNull CommandData data, @NonNull Function<Player, String> mapper) {

        int limit = this.getLimit(argument, invocation);
        CommandSender sender = data.get("sender", CommandSender.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;

        boolean completeSelf = this.getData(argument, invocation,
            "self", () -> true,
            Boolean::parseBoolean
        );

        return this.filter(limit, this.stringFilter(invocation), Bukkit.getServer().getOnlinePlayers().stream()
            .filter(onlinePlayer -> {
                // non-player senders complete all players
                if (player == null) {
                    return true;
                }
                // sender should be hidden when self=false
                if (!completeSelf && onlinePlayer.equals(player)) {
                    return false;
                }
                // sender should not complete hidden players
                if (!player.canSee(onlinePlayer) && !sender.hasPermission("okaeri.commands.invisible")) {
                    return false;
                }
                // complete otherwise
                return true;
            })
            .map(mapper));
    }

    protected List<String> completeEnchantment(@NonNull Invocation invocation, int limit) {
        return this.filter(limit, this.stringFilter(invocation), Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase));
    }

    protected List<String> completePotionEffectType(@NotNull Invocation invocation, int limit) {
        return this.filter(limit, this.stringFilter(invocation), Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).map(String::toLowerCase));
    }

    protected List<String> completeWorld(@NotNull Invocation invocation, int limit) {
        return this.filter(limit, this.stringFilter(invocation), Bukkit.getWorlds().stream().map(World::getName));
    }

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull Invocation invocation, @NonNull CommandData data) {

        Class<?> type = argument.getType();
        int limit = this.getLimit(argument, invocation);

        if (OfflinePlayer.class.isAssignableFrom(type)) {
            return this.completePlayer(argument, invocation, data, HumanEntity::getName);
        }

        if (Enchantment.class.isAssignableFrom(type)) {
            return this.completeEnchantment(invocation, limit);
        }

        if (PotionEffectType.class.isAssignableFrom(type)) {
            return this.completePotionEffectType(invocation, limit);
        }

        if (World.class.isAssignableFrom(type)) {
            return this.completeWorld(invocation, limit);
        }

        return super.complete(argument, invocation, data);
    }
}
