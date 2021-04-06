package eu.okaeri.commands.bukkit.response;

import net.md_5.bungee.api.ChatColor;

public class ErrorResponse extends RawResponse {

    public static ColorResponse of(String... value) {
        return ColorResponse.of(ChatColor.RED, value);
    }
}
