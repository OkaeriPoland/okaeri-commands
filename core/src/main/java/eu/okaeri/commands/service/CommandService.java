package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.InvocationMeta;
import lombok.NonNull;

public interface CommandService {

    default void preResolve(@NonNull Invocation invocation, @NonNull CommandData data) {
    }

    default void preInvoke(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull InvocationMeta invocationMeta) {
    }

    default Object postInvoke(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull InvocationMeta invocationMeta, Object result) {
        return result;
    }
}
