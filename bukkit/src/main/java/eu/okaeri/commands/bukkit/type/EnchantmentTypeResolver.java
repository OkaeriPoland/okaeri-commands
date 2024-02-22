package eu.okaeri.commands.bukkit.type;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class EnchantmentTypeResolver extends BasicTypeResolver<Enchantment> {

    private static MethodHandle EnchantmentGetById;

    static {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        try {
            EnchantmentGetById = lookup.unreflect(Enchantment.class.getMethod("getById", int.class));
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
            EnchantmentGetById = null;
        }
    }

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Enchantment.class.isAssignableFrom(type);
    }

    @Override
    public Enchantment resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {

        Enchantment enchantment = Enchantment.getByName(text);
        if (enchantment != null) {
            return enchantment;
        }

        if (EnchantmentGetById == null) {
            return null;
        }

        try {
            int id = Integer.parseInt(text);
            return (Enchantment) EnchantmentGetById.invoke(id);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
}
