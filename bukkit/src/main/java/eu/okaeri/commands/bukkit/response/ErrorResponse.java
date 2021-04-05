package eu.okaeri.commands.bukkit.response;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ErrorResponse extends RawResponse {

    protected ErrorResponse(String... value) {
        super(value);
    }

    public static ErrorResponse of(String... value) {
        return new ErrorResponse(value);
    }

    @Override
    public String render() {
        return Arrays.stream(this.raw())
                .map(line -> ChatColor.RED + line)
                .collect(Collectors.joining("\n"));
    }
}
