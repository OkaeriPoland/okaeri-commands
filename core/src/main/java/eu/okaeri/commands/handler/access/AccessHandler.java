package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface AccessHandler {

    default boolean allowAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data) {
        return this.allowAccess(service, invocation, data, true);
    }

    boolean allowAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors);

    default void checkAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data) {
        this.checkAccess(service, invocation, data, true);
    }

    void checkAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors);

    boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data);

    void checkAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data);

    default boolean allowCall(@NonNull InvocationMeta invocationMeta) {
        return true;
    }

    default void checkCall(@NonNull InvocationMeta invocationMeta) {
    }
}
