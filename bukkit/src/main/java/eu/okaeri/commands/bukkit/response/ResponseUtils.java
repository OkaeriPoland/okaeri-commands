package eu.okaeri.commands.bukkit.response;

import lombok.NonNull;

import java.util.Map;

final class ResponseUtils {

    public static String replaceAll(@NonNull String in, @NonNull Map<String, String> map) {

        for (Map.Entry<String, String> entry : map.entrySet()) {
            in = in.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return in;
    }
}
