package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.InvocationMeta;
import lombok.NonNull;

public interface CommandService {

    default void preResolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
    }

    default void preInvoke(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull InvocationMeta invocationMeta) {
    }

    default Object postInvoke(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull InvocationMeta invocationMeta, Object result) {
        return result;
    }
}
