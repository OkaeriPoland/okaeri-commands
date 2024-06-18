package eu.okaeri.commands.velocity.response;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface VelocityResponse {

    static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
        .preProcessor(text -> Pattern.compile("§([0-9A-Fa-fK-Ok-oRXrx])").matcher(text).replaceAll("&$1")) // convert section to ampersand
        .postProcessor(component -> component.replaceText(TextReplacementConfig.builder()
            .match(".*")
            .replacement((result, input) -> LegacyComponentSerializer.legacyAmpersand().deserialize(result.group()))
            .build()))
        .build();

    static RawResponse raw(@NonNull String... value) {
        return RawResponse.of(value);
    }

    static ColorResponse color(@NonNull TextColor color, @NonNull String... value) {
        return ColorResponse.of(color, value);
    }

    static ColorResponse ok(@NonNull String... value) {
        return color(NamedTextColor.GREEN, value);
    }

    static ColorResponse err(@NonNull String... value) {
        return color(NamedTextColor.RED, value);
    }

    Component render();

    String raw();

    VelocityResponse with(@NonNull String field, String value);

    default VelocityResponse sendTo(@NonNull CommandSource target) {
        target.sendMessage(this.render());
        return this;
    }

    default VelocityResponse sendTo(@NonNull Collection<? extends CommandSource> targets) {
        Component render = this.render();
        targets.forEach(target -> target.sendMessage(render));
        return this;
    }

    default VelocityResponse sendToAllPlayers(ProxyServer proxy) {
        return this.sendTo(new ArrayList<>(proxy.getAllPlayers()));
    }

    default VelocityResponse sendToAllWithPermission(ProxyServer proxy, String permission) {
        return this.sendTo(new ArrayList<>(proxy.getAllPlayers()).stream()
            .filter(player -> player.hasPermission(permission))
            .collect(Collectors.toList()));
    }

    default VelocityResponse sendToConsole(ProxyServer proxy) {
        return this.sendTo(proxy.getConsoleCommandSource());
    }

    default VelocityResponse with(@NonNull String field, @NonNull Double value) {
        return this.with(field, String.format(Locale.US, "%.2f", value));
    }

    default VelocityResponse with(@NonNull String field, @NonNull CommandSource value) {
        if (value instanceof Player) {
            return this.with(field, ((Player) value).getUsername());
        } else if (value instanceof ConsoleCommandSource) {
            return this.with(field, "CONSOLE");
        }
        return this.with(field, value.toString()); // heh
    }
}
