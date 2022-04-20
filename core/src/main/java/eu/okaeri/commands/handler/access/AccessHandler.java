package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public interface AccessHandler {

    default boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return this.allowAccess(service, invocationContext, commandContext, true);
    }

    boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors);

    default void checkAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        this.checkAccess(service, invocationContext, commandContext, true);
    }

    void checkAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors);

    boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    void checkAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    default boolean allowCall(@NonNull InvocationMeta invocationMeta) {
        return true;
    }

    default void checkCall(@NonNull InvocationMeta invocationMeta) {
    }
}
