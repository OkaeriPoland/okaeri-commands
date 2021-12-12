package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BukkitAccessHandler implements AccessHandler {

    public final Commands commands;

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors) {

        if (!commandContext.has("sender", CommandSender.class)) {
            throw new RuntimeException("Cannot process command without sender in the context!");
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender.isOp() || sender.hasPermission("*")) {
            // SPEEEEEEEED
            return true;
        }

        if (checkExecutors) {
            boolean noExecutorAccess = this.commands.findByLabel(service.getLabel()).stream()
                .noneMatch(command -> this.allowAccess(command.getExecutor(), invocationContext, commandContext));
            if (noExecutorAccess) {
                return false;
            }
        }

        Set<String> permissions = this.getPermissions(service, invocationContext, commandContext);
        return this.hasPermissions(sender, permissions, this.getMode(service));
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors) {

        if (this.allowAccess(service, invocationContext, commandContext, checkExecutors)) {
            return;
        }

        String[] perms = this.getPermissions(service, invocationContext, commandContext).toArray(new String[0]);
        Permission.Mode mode = this.getMode(service);

        if (perms.length == 0) {
            throw new NoAccessException("");
        }

        switch (mode) {
            case ANY:
                throw new NoAccessException(perms[0]);
            case ALL:
                throw new NoAccessException(String.join(", ", perms));
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        if (!commandContext.has("sender", CommandSender.class)) {
            throw new RuntimeException("Cannot process command without sender in the context!");
        }

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender.isOp() || sender.hasPermission("*")) {
            // SPEEEEEEEED
            return true;
        }

        Set<String> permissions = this.getPermissions(executor, invocationContext, commandContext);
        return this.hasPermissions(sender, permissions, this.getMode(executor));
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {

        if (this.allowAccess(executor, invocationContext, commandContext)) {
            return;
        }

        String[] perms = this.getPermissions(executor, invocationContext, commandContext).toArray(new String[0]);
        Permission.Mode mode = this.getMode(executor);

        if (perms.length == 0) {
            throw new NoAccessException("");
        }

        switch (mode) {
            case ANY:
                throw new NoAccessException(perms[0]);
            case ALL:
                throw new NoAccessException(String.join(", ", perms));
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    protected Set<String> getPermissions(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return this.getPermissions(service.getImplementor().getClass().getAnnotation(Permission.class), invocationContext, commandContext);
    }

    protected Set<String> getPermissions(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return this.getPermissions(executor.getMethod().getAnnotation(Permission.class), invocationContext, commandContext);
    }

    protected Set<String> getPermissions(Permission permission, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return (permission == null) ? Collections.emptySet() : Arrays.stream(permission.value())
            .map(perm -> this.commands.resolveText(invocationContext, commandContext, perm))
            .collect(Collectors.toSet());

    }

    protected Permission.Mode getMode(@NonNull ServiceMeta service) {
        return this.getMode(service.getImplementor().getClass().getAnnotation(Permission.class));
    }

    protected Permission.Mode getMode(@NonNull ExecutorMeta executor) {
        return this.getMode(executor.getMethod().getAnnotation(Permission.class));
    }

    protected Permission.Mode getMode(Permission permission) {
        return (permission == null) ? Permission.Mode.ALL : permission.mode();
    }

    protected boolean hasPermissions(@NonNull CommandSender sender, @NonNull Set<String> permissions, @NonNull Permission.Mode mode) {
        if (permissions.isEmpty()) {
            return true;
        }
        switch (mode) {
            case ANY:
                for (String servicePermission : permissions) {
                    if (sender.hasPermission(servicePermission)) {
                        return true;
                    }
                }
                return false;
            case ALL:
                for (String servicePermission : permissions) {
                    if (!sender.hasPermission(servicePermission)) {
                        return false;
                    }
                }
                return true;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }
}
