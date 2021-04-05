package eu.okaeri.commands.bukkit.response;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ColorResponse extends RawResponse {

    private ChatColor color;

    protected ColorResponse(ChatColor color, String... value) {
        super(value);
        this.color = color;
    }

    public static ColorResponse of(String... value) {
        return new ColorResponse(null, value);
    }

    public static ColorResponse of(ChatColor color, String... value) {
        return new ColorResponse(color, value);
    }

    public static ColorResponse of(org.bukkit.ChatColor color, String... value) {
        return new ColorResponse(ChatColor.valueOf(color.name()), value);
    }

    @Override
    public String render() {

        if (this.color != null) {
            return Arrays.stream(this.raw())
                    .map(line -> this.color + line)
                    .collect(Collectors.joining("\n"));
        }

        return ChatColor.translateAlternateColorCodes('&', super.render());
    }
}
