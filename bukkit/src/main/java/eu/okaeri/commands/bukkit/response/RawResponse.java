package eu.okaeri.commands.bukkit.response;

import eu.okaeri.commands.bukkit.response.placeholder.Placeholers;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.LinkedHashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class RawResponse implements BukkitResponse {

    private String value;
    private Map<String, String> fields = new LinkedHashMap<>();

    protected RawResponse(String... value) {
        this.value = String.join("\n", value);
    }

    public static RawResponse of(String... value) {
        return new RawResponse(value);
    }

    @Override
    public String raw() {
        return this.value;
    }

    public Map<String, String> fields() {
        return new LinkedHashMap<>(this.fields);
    }

    @Override
    public BukkitResponse withField(String field, String value) {
        if (field == null) throw new IllegalArgumentException("field cannot be null");
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        this.fields.put(field, value);
        return this;
    }

    @Override
    public BaseComponent[] render() {
        TextComponent component = new TextComponent();
        component.setText(Placeholers.replaceAll(this.raw(), this.fields()));
        return new TextComponent[]{component};
    }
}
