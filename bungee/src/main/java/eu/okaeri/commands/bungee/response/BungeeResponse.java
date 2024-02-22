package eu.okaeri.commands.bungee.response;

import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public interface BungeeResponse {

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

    BungeeResponse with(@NonNull String field, String value);

    default BungeeResponse sendTo(@NonNull CommandSender target) {
        target.sendMessage(this.render());
        return this;
    }

    default BungeeResponse sendTo(@NonNull Collection<? extends CommandSender> targets) {
        String render = this.render();
        targets.forEach(target -> target.sendMessage(render));
        return this;
    }

    default BungeeResponse sendToAllPlayers() {
        return this.sendTo(new ArrayList<>(ProxyServer.getInstance().getPlayers()));
    }

    default BungeeResponse sendToAllWithPermission(String permission) {
        return this.sendTo(new ArrayList<>(ProxyServer.getInstance().getPlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    default BungeeResponse sendToConsole() {
        return this.sendTo(ProxyServer.getInstance().getConsole());
    }

    default BungeeResponse with(@NonNull String field, @NonNull Double value) {
        return this.with(field, String.format(Locale.US, "%.2f", value));
    }

    default BungeeResponse with(@NonNull String field, @NonNull CommandSender value) {
        return this.with(field, value.getName());
    }
}
