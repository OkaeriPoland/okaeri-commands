package eu.okaeri.commands.bukkit.response;

import org.bukkit.ChatColor;

public class ErrorResponse extends RawResponse {

    public static ColorResponse of(String... value) {
        return ColorResponse.of(ChatColor.RED, value);
    }
}
