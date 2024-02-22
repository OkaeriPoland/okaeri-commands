package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public interface BukkitResponse {

    static RawResponse raw(@NonNull String... value) {
        return RawResponse.of(value);
    }

    static ColorResponse color(@NonNull ChatColor color, @NonNull String... value) {
        return ColorResponse.of(color, value);
    }

    static ColorResponse ok(@NonNull String... value) {
        return color(ChatColor.GREEN, value);
    }

    static ColorResponse err(@NonNull String... value) {
        return color(ChatColor.RED, value);
    }

    String render();

    String raw();

    BukkitResponse with(@NonNull String field, String value);

    /**
     * @deprecated Use {@link #with(String, String)} instead.
     */
    @Deprecated
    default BukkitResponse withField(@NonNull String field, String value) {
        return this.with(field, value);
    }

    default BukkitResponse sendTo(@NonNull CommandSender target) {
        target.sendMessage(this.render());
        return this;
    }

    default BukkitResponse sendTo(@NonNull Collection<? extends CommandSender> targets) {
        String render = this.render();
        targets.forEach(target -> target.sendMessage(render));
        return this;
    }

    default BukkitResponse sendToAllPlayers() {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    default BukkitResponse sendToAllWithPermission(@NonNull String permission) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    default BukkitResponse sendToAllPlayersThatCanSee(@NonNull Player player) {
        return this.sendTo(new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
            .filter(onlinePlayer -> onlinePlayer.canSee(player))
            .collect(Collectors.toList()));
    }

    default BukkitResponse sendToConsole() {
        return this.sendTo(Bukkit.getConsoleSender());
    }

    default BukkitResponse with(@NonNull String field, @NonNull Double value) {
        return this.with(field, String.format(Locale.US, "%.2f", value));
    }

    /**
     * @deprecated Use {@link #with(String, Double)} instead.
     */
    @Deprecated
    default BukkitResponse withField(@NonNull String field, @NonNull Double value) {
        return this.with(field, value);
    }

    default BukkitResponse with(@NonNull String field, @NonNull CommandSender value) {
        return this.with(field, value.getName());
    }

    /**
     * @deprecated Use {@link #with(String, CommandSender)} instead.
     */
    @Deprecated
    default BukkitResponse withField(@NonNull String field, @NonNull CommandSender value) {
        return this.with(field, value);
    }

    default BukkitResponse with(@NonNull String field, @NonNull World value) {
        return this.with(field, value.getName());
    }

    /**
     * @deprecated Use {@link #with(String, World)} instead.
     */
    @Deprecated
    default BukkitResponse withField(@NonNull String field, @NonNull World value) {
        return this.with(field, value);
    }

    default BukkitResponse with(@NonNull String field, @NonNull ItemStack value) {

        String result = value.getType().name().replace("_", " ");

        if (value.hasItemMeta() && (value.getItemMeta().getDisplayName() != null)) {
            result += " ('" + value.getItemMeta().getDisplayName() + "')";
        }

        return this.with(field, result);
    }

    /**
     * @deprecated Use {@link #with(String, ItemStack)} instead.
     */
    @Deprecated
    default BukkitResponse withField(@NonNull String field, @NonNull ItemStack value) {
        return this.with(field, value);
    }
}
