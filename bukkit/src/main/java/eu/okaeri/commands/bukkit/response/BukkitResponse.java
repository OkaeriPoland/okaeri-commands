package eu.okaeri.commands.bukkit.response;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public interface BukkitResponse {

    String render();

    String[] raw();

    BukkitResponse withField(String field, String value);

    default BukkitResponse sendTo(CommandSender target) {
        target.sendMessage(this.render());
        return this;
    }

    default BukkitResponse sendTo(Collection<? extends CommandSender> targets) {
        String render = this.render();
        targets.forEach(target -> target.sendMessage(render));
        return this;
    }

    default BukkitResponse sendToAllPlayers() {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    default BukkitResponse sendToAllWithPermission(String permission) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                .filter(player -> player.hasPermission(permission))
                .collect(Collectors.toList()));
    }

    default BukkitResponse sendToAllPlayersThatCanSee(Player player) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                .filter(onlinePlayer -> onlinePlayer.canSee(player))
                .collect(Collectors.toList()));
    }

    default BukkitResponse sendToConsole() {
        return this.sendTo(Bukkit.getConsoleSender());
    }

    default BukkitResponse withField(String field, Double value) {
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        return this.withField(field, String.format(Locale.US, "%.2f", value));
    }

    default BukkitResponse withField(String field, CommandSender value) {
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        return this.withField(field, value.getName());
    }

    default BukkitResponse withField(String field, World value) {
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        return this.withField(field, value.getName());
    }

    default BukkitResponse withField(String field, ItemStack value) {

        if (value == null) throw new IllegalArgumentException("value cannot be null");
        String result = value.getType().name().replace("_", " ");

        if (value.hasItemMeta() && (value.getItemMeta().getDisplayName() != null)) {
            result += " ('" + value.getItemMeta().getDisplayName() + "')";
        }

        return this.withField(field, result);
    }
}
