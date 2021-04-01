package eu.okaeri.commands.bukkit.response;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SuccessResponse extends RawResponse {

    protected SuccessResponse(String... value) {
        super(value);
    }

    public static SuccessResponse of(String... value) {
        return new SuccessResponse(value);
    }

    @Override
    public String render() {
        return Arrays.stream(this.getValue())
                .map(line -> ChatColor.GREEN + line)
                .collect(Collectors.joining("\n"));
    }
}
