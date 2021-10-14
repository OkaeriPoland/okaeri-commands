package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class BukkitCompletionHandler extends DefaultCompletionHandler {

    @Override
    public List<String> complete(@NonNull ArgumentMeta argument, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        Class<?> type = this.resolveType(argument);
        Predicate<String> stringFilter = this.stringFilter(invocationContext);

        if (OfflinePlayer.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName));
        }

        if (Enchantment.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, Arrays.stream(Enchantment.values()).map(Enchantment::getName));
        }

        if (PotionEffectType.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName));
        }

        if (World.class.isAssignableFrom(type)) {
            return this.filter(stringFilter, Bukkit.getWorlds().stream().map(World::getName));
        }

        return super.complete(argument, invocationContext, commandContext);
    }
}
