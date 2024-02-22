package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MultiAccessHandler implements AccessHandler {

    private final List<AccessHandler> handlers;

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors) {
        return this.handlers.stream().allMatch(handler -> handler.allowAccess(service, invocation, data, checkExecutors));
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors) {
        this.handlers.forEach(handler -> handler.checkAccess(service, invocation, data, checkExecutors));
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data) {
        return this.handlers.stream().allMatch(handler -> handler.allowAccess(executor, invocation, data));
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data) {
        this.handlers.forEach(handler -> handler.checkAccess(executor, invocation, data));
    }

    @Override
    public boolean allowCall(@NonNull InvocationMeta invocationMeta) {
        return this.handlers.stream().allMatch(handler -> handler.allowCall(invocationMeta));
    }

    @Override
    public void checkCall(@NonNull InvocationMeta invocationMeta) {
        this.handlers.forEach(handler -> handler.checkCall(invocationMeta));
    }
}
