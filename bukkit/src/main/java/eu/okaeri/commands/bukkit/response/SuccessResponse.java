package eu.okaeri.commands.bukkit.response;

import net.md_5.bungee.api.ChatColor;

public final class SuccessResponse {

    public static ColorResponse of(String... value) {
        return ColorResponse.of(ChatColor.GREEN, value);
    }
}
