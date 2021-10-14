package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BukkitAccessHandler implements AccessHandler {

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        if (!commandContext.has("sender", CommandSender.class)) {
            throw new RuntimeException("Cannot process command without sender in the context!");
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        return this.hasPermissions(sender, this.getPermissions(service));
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        if (!this.allowAccess(service, invocationContext, commandContext)) {
            throw new NoAccessException(this.getPermissions(service).toArray(new String[0])[0]);
        }
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        if (invocationContext.getService() != null) {
            if (!this.allowAccess(invocationContext.getService(), invocationContext, commandContext)) {
                return false;
            }
        }

        if (!commandContext.has("sender", CommandSender.class)) {
            throw new RuntimeException("Cannot process command without sender in the context!");
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        return this.hasPermissions(sender, this.getPermissions(executor));
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        if (!this.allowAccess(executor, invocationContext, commandContext)) {
            throw new NoAccessException(this.getPermissions(executor).toArray(new String[0])[0]);
        }
    }

    private Set<String> getPermissions(@NonNull ServiceMeta service) {
        Permission permission = service.getImplementor().getClass().getAnnotation(Permission.class);
        return (permission == null) ? Collections.emptySet() : new HashSet<>(Arrays.asList(permission.value()));
    }

    private Set<String> getPermissions(@NonNull ExecutorMeta executor) {
        Permission permission = executor.getMethod().getAnnotation(Permission.class);
        return (permission == null) ? Collections.emptySet() : new HashSet<>(Arrays.asList(permission.value()));
    }

    private boolean hasPermissions(@NonNull CommandSender sender, @NonNull Set<String> permissions) {

        if (permissions.isEmpty()) {
            return true;
        }

        for (String servicePermission : permissions) {
            if (sender.hasPermission(servicePermission)) {
                return true;
            }
        }

        return false;
    }
}
