package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;
import org.bukkit.ChatColor;

public class ErrorResponse extends RawResponse {

    public static ColorResponse of(@NonNull String... value) {
        return ColorResponse.of(ChatColor.RED, value);
    }
}
