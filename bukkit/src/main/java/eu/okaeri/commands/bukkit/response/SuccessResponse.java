package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;
import org.bukkit.ChatColor;

public final class SuccessResponse {

    public static ColorResponse of(@NonNull String... value) {
        return ColorResponse.of(ChatColor.GREEN, value);
    }
}
