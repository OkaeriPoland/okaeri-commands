package eu.okaeri.commands.bukkit.response;

import eu.okaeri.commands.bukkit.response.placeholder.Placeholers;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
    public BaseComponent[] render() {

        if (this.color == null) {
            String render = ChatColor.translateAlternateColorCodes('&', super.raw());
            render = Placeholers.replaceAll(render, this.fields());
            return TextComponent.fromLegacyText(render);
        }

        TextComponent component = new TextComponent();
        component.setText(Placeholers.replaceAll(this.raw(), this.fields()));
        component.setColor(this.color);
        return new TextComponent[]{component};
    }
}
