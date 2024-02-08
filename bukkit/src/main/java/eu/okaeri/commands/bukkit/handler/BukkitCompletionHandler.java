package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
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
        commands.registerCompletion("bukkit:player:name", (completion, argument, invocationContext, commandContext) ->
            this.completePlayer(argument, invocationContext, commandContext, HumanEntity::getName));
        commands.registerCompletion("bukkit:player:address", (completion, argument, invocationContext, commandContext) ->
            this.completePlayer(argument, invocationContext, commandContext, player -> player.getAddress().getAddress().getHostAddress()));
        commands.registerCompletion("bukkit:player:uuid", (completion, argument, invocationContext, commandContext) ->
            this.completePlayer(argument, invocationContext, commandContext, player -> player.getUniqueId().toString()));
        commands.registerCompletion("bukkit:enchantment", (completion, argument, invocationContext, commandContext) ->
            this.completeEnchantment(invocationContext, this.getLimit(argument, invocationContext)));
        commands.registerCompletion("bukkit:potioneffecttype", (completion, argument, invocationContext, commandContext) ->
            this.completePotionEffectType(invocationContext, this.getLimit(argument, invocationContext)));
        commands.registerCompletion("bukkit:world", (completion, argument, invocationContext, commandContext) ->
            this.completeWorld(invocationContext, this.getLimit(argument, invocationContext)));
    }

    protected List<String> completePlayer(@NotNull ArgumentMeta argument, @NotNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull Function<Player, String> mapper) {

        int limit = this.getLimit(argument, invocationContext);
        CommandSender sender = commandContext.get("sender", CommandSender.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;

        boolean completeSelf = this.getData(argument, invocationContext,
            "self", () -> true,
            Boolean::parseBoolean
        );

        return this.filter(limit, this.stringFilter(invocationContext), Bukkit.getServer().getOnlinePlayers().stream()
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

    protected List<String> completeEnchantment(@NonNull InvocationContext invocationContext, int limit) {
        return this.filter(limit, this.stringFilter(invocationContext), Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase));
    }

    protected List<String> completePotionEffectType(@NotNull InvocationContext invocationContext, int limit) {
        return this.filter(limit, this.stringFilter(invocationContext), Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).map(String::toLowerCase));
    }

    protected List<String> completeWorld(@NotNull InvocationContext invocationContext, int limit) {
        return this.filter(limit, this.stringFilter(invocationContext), Bukkit.getWorlds().stream().map(World::getName));
    }

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = argument.getType();
        int limit = this.getLimit(argument, invocationContext);

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;

        if (OfflinePlayer.class.isAssignableFrom(type)) {
            return this.completePlayer(argument, invocationContext, commandContext, HumanEntity::getName);
        }

        if (Enchantment.class.isAssignableFrom(type)) {
            return this.completeEnchantment(invocationContext, limit);
        }

        if (PotionEffectType.class.isAssignableFrom(type)) {
            return this.completePotionEffectType(invocationContext, limit);
        }

        if (World.class.isAssignableFrom(type)) {
            return this.completeWorld(invocationContext, limit);
        }

        return super.complete(argument, invocationContext, commandContext);
    }
}
