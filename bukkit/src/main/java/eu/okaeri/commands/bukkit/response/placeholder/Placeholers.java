package eu.okaeri.commands.bukkit.response.placeholder;

import java.util.Map;

public final class Placeholers {

    public static String replaceAll(String in, Map<String, String> map) {

        for (Map.Entry<String, String> entry : map.entrySet()) {
            in = in.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return in;
    }
}
