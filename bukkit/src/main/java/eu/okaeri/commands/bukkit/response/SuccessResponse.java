package eu.okaeri.commands.bukkit.response;

import org.bukkit.ChatColor;

public final class SuccessResponse {

    public static ColorResponse of(String... value) {
        return ColorResponse.of(ChatColor.GREEN, value);
    }
}
