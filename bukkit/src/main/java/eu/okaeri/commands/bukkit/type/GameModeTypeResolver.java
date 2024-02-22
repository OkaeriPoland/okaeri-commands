package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import eu.okaeri.commands.type.resolver.EnumTypeResolver;
import lombok.NonNull;
import org.bukkit.GameMode;

import java.util.HashMap;
import java.util.Map;

public class GameModeTypeResolver extends BasicTypeResolver<GameMode> {

    private static final EnumTypeResolver ENUM_TYPE_RESOLVER = new EnumTypeResolver();
    private static final Map<String, GameMode> ID_TO_TYPE = new HashMap<>();

    static {
        ID_TO_TYPE.put("0", GameMode.SURVIVAL);
        ID_TO_TYPE.put("1", GameMode.CREATIVE);
        ID_TO_TYPE.put("2", GameMode.ADVENTURE);
        ID_TO_TYPE.put("3", GameMode.SPECTATOR);
    }

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return GameMode.class.isAssignableFrom(type);
    }

    @Override
    public GameMode resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {

        GameMode mode = ID_TO_TYPE.get(text);
        if (mode != null) {
            return mode;
        }

        return (GameMode) ENUM_TYPE_RESOLVER.resolve(invocation, data, argumentMeta, text);
    }
}
