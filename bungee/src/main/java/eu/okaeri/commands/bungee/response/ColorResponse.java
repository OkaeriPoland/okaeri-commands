package eu.okaeri.commands.bungee.response;

import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;

public class ColorResponse extends RawResponse {

    private ChatColor color;

    protected ColorResponse(ChatColor color, @NonNull String... value) {
        super(value);
        this.color = color;
    }

    public static ColorResponse of(@NonNull String... value) {
        return new ColorResponse(null, value);
    }

    public static ColorResponse of(ChatColor color, @NonNull String... value) {
        return new ColorResponse(color, value);
    }

    @Override
    public String render() {

        if (this.color == null) {
            String render = ChatColor.translateAlternateColorCodes('&', super.raw());
            render = replaceAll(render, this.fields());
            return render;
        }

        return this.color + replaceAll(this.raw(), this.fields());
    }
}
