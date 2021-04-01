package eu.okaeri.commands.bukkit.response;

import lombok.Data;

@Data
public class RawResponse implements BukkitResponse {

    private final String[] value;

    protected RawResponse(String... value) {
        this.value = value;
    }

    public static RawResponse of(String... value) {
        return new RawResponse(value);
    }

    @Override
    public String render() {
        return String.join("\n", this.value);
    }
}
