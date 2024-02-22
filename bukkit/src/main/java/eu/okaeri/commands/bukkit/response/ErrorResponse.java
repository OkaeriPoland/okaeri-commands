package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;
import org.bukkit.ChatColor;

/**
 * @deprecated Use {@link BukkitResponse#err(String...)} instead.
 */
@Deprecated
public final class ErrorResponse {

    public static ColorResponse of(@NonNull String... value) {
        return ColorResponse.of(ChatColor.RED, value);
    }
}
