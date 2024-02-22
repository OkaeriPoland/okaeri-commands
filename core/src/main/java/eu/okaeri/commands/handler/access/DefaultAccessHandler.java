package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public class DefaultAccessHandler implements AccessHandler {

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors) {
        return true;
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors) {
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data) {
        return true;
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data) {
    }
}
