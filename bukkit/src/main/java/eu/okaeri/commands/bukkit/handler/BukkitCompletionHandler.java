package eu.okaeri.commands.bukkit.handler;

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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
public class BukkitCompletionHandler extends DefaultCompletionHandler {

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = argument.getType();
        int limit = this.getLimit(argument, invocationContext);
        Predicate<String> stringFilter = this.stringFilter(invocationContext);

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;

        if (OfflinePlayer.class.isAssignableFrom(type)) {

            boolean completeSelf = this.getData(argument, invocationContext,
                "self", () -> true,
                Boolean::parseBoolean
            );

            return this.filter(limit, stringFilter, Bukkit.getServer().getOnlinePlayers().stream()
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
                .map(HumanEntity::getName));
        }

        if (Enchantment.class.isAssignableFrom(type)) {
            return this.filter(limit, stringFilter, Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase));
        }

        if (PotionEffectType.class.isAssignableFrom(type)) {
            return this.filter(limit, stringFilter, Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).map(String::toLowerCase));
        }

        if (World.class.isAssignableFrom(type)) {
            return this.filter(limit, stringFilter, Bukkit.getWorlds().stream().map(World::getName));
        }

        return super.complete(argument, invocationContext, commandContext);
    }
}
