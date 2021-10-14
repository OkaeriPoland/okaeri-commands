package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;

public class DefaultAccessHandler implements AccessHandler {

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return true;
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta serviceMeta, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return true;
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
    }
}
