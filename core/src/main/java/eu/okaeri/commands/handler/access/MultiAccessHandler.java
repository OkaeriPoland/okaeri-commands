package eu.okaeri.commands.handler.access;

import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MultiAccessHandler implements AccessHandler {

    private final List<AccessHandler> handlers;

    @Override
    public boolean allowAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors) {
        return this.handlers.stream().allMatch(handler -> handler.allowAccess(service, invocationContext, commandContext, checkExecutors));
    }

    @Override
    public void checkAccess(@NonNull ServiceMeta service, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, boolean checkExecutors) {
        this.handlers.forEach(handler -> handler.checkAccess(service, invocationContext, commandContext, checkExecutors));
    }

    @Override
    public boolean allowAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        return this.handlers.stream().allMatch(handler -> handler.allowAccess(executor, invocationContext, commandContext));
    }

    @Override
    public void checkAccess(@NonNull ExecutorMeta executor, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext) {
        this.handlers.forEach(handler -> handler.checkAccess(executor, invocationContext, commandContext));
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
