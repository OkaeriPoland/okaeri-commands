package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import org.bukkit.potion.PotionEffectType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class PotionEffectTypeResolver extends BasicTypeResolver<org.bukkit.potion.PotionEffectType> {

    private static MethodHandle PotionEffectTypeGetById;

    static {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        try {
            PotionEffectTypeGetById = lookup.unreflect(PotionEffectType.class.getMethod("getById", int.class));
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
            PotionEffectTypeGetById = null;
        }
    }

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return PotionEffectType.class.isAssignableFrom(type);
    }

    @Override
    public PotionEffectType resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {

        PotionEffectType effectType = PotionEffectType.getByName(text);
        if (effectType != null) {
            return effectType;
        }

        if (PotionEffectTypeGetById == null) {
            return null;
        }

        try {
            int id = Integer.parseInt(text);
            return (PotionEffectType) PotionEffectTypeGetById.invoke(id);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
}
