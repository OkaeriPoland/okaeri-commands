package eu.okaeri.commands.bukkit.response;

import eu.okaeri.commands.bukkit.response.placeholder.Placeholers;
import org.bukkit.ChatColor;

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

    @Override
    public String render() {

        if (this.color == null) {
            String render = ChatColor.translateAlternateColorCodes('&', super.raw());
            render = Placeholers.replaceAll(render, this.fields());
            return render;
        }

        return this.color + Placeholers.replaceAll(this.raw(), this.fields());
    }
}
