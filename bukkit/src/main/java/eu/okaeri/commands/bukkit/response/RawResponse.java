package eu.okaeri.commands.bukkit.response;

import eu.okaeri.commands.bukkit.response.placeholder.Placeholers;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class RawResponse implements BukkitResponse {

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
    public BukkitResponse withField(@NonNull String field, @NonNull String value) {
        this.fields.put(field, value);
        return this;
    }

    @Override
    public String render() {
        return Placeholers.replaceAll(this.raw(), this.fields());
    }
}
