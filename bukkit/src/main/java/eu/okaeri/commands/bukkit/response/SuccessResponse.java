package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;
import org.bukkit.ChatColor;

/**
 * @deprecated Use {@link BukkitResponse#ok(String...)} instead.
 */
@Deprecated
public final class SuccessResponse {

    public static ColorResponse of(@NonNull String... value) {
        return ColorResponse.of(ChatColor.GREEN, value);
    }
}
