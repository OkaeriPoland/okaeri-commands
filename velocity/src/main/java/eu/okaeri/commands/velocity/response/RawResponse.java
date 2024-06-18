package eu.okaeri.commands.velocity.response;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.kyori.adventure.text.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class RawResponse implements VelocityResponse {

    private String value;
    private Map<String, String> fields = new LinkedHashMap<>();

    protected RawResponse(@NonNull String... value) {
        this.value = String.join("\n", value);
    }

    public static RawResponse of(@NonNull String... value) {
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
    public VelocityResponse with(@NonNull String field, @NonNull String value) {
        this.fields.put(field, value);
        return this;
    }

    @Override
    public Component render() {
        String replaced = replaceAll(this.raw(), this.fields());
        return Component.text(replaced);
    }

    protected static String replaceAll(@NonNull String in, @NonNull Map<String, String> map) {

        for (Map.Entry<String, String> entry : map.entrySet()) {
            in = in.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return in;
    }
}
