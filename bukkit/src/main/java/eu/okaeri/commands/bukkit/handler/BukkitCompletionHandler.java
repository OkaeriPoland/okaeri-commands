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

public class BukkitCompletionHandler extends DefaultCompletionHandler {

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = argument.getType();
        Predicate<String> stringFilter = this.stringFilter(invocationContext);
        CommandSender sender = commandContext.get("sender", CommandSender.class);
        Player player = (sender instanceof Player) ? ((Player) sender) : null;
        int limit = this.getLimit(argument, invocationContext);

        if (OfflinePlayer.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, limit, Bukkit.getServer().getOnlinePlayers().stream()
                .filter(onlinePlayer -> (player == null) || player.canSee(onlinePlayer) || sender.hasPermission("okaeri.commands.invisible"))
                .map(HumanEntity::getName));
        }

        if (Enchantment.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, limit, Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase));
        }

        if (PotionEffectType.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, limit, Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).map(String::toLowerCase));
        }

        if (World.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, limit, Bukkit.getWorlds().stream().map(World::getName));
        }

        return super.complete(argument, invocationContext, commandContext);
    }
}
