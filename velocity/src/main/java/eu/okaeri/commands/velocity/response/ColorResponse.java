package eu.okaeri.commands.velocity.response;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ColorResponse extends RawResponse {

    private TextColor color;

    protected ColorResponse(TextColor color, @NonNull String... value) {
        super(value);
        this.color = color;
    }

    public static ColorResponse of(@NonNull String... value) {
        return new ColorResponse(null, value);
    }

    public static ColorResponse of(TextColor color, @NonNull String... value) {
        return new ColorResponse(color, value);
    }

    @Override
    public Component render() {
        String color = (this.color == null) ? "" : ("<" + this.color + ">");
        String replaced = replaceAll(this.raw(), this.fields());
        return MINI_MESSAGE.deserialize(color + replaced); // FIXME: unsafe minimessage input with user supplied fields
    }
}
