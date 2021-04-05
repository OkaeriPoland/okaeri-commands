package eu.okaeri.commands.bukkit.response;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;

@ToString
@EqualsAndHashCode
public class RawResponse implements BukkitResponse {

    private String[] value;

    protected RawResponse(String... value) {
        this.value = value;
    }

    public static RawResponse of(String... value) {
        return new RawResponse(value);
    }

    @Override
    public String[] raw() {
        return this.value;
    }

    @Override
    public BukkitResponse withField(String field, String value) {

        if (field == null) throw new IllegalArgumentException("field cannot be null");
        if (value == null) throw new IllegalArgumentException("value cannot be null");

        this.value = Arrays.stream(this.raw())
                .map(line -> line.replace(field, value))
                .toArray(String[]::new);

        return this;
    }

    @Override
    public String render() {
        return String.join("\n", this.raw());
    }
}
