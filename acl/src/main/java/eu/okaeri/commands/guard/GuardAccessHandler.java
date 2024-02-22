package eu.okaeri.commands.guard;

import eu.okaeri.acl.guardian.Guardian;
import eu.okaeri.acl.guardian.GuardianContext;
import eu.okaeri.acl.guardian.GuardianViolation;
import eu.okaeri.commands.exception.NoAccessException;
import eu.okaeri.commands.guard.context.GuardianContextProvider;
import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GuardAccessHandler implements AccessHandler {

    protected final @NonNull Guardian guardian;
    protected final @NonNull GuardianContextProvider contextProvider;

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

    @Override
    public boolean allowCall(@NonNull InvocationMeta invocationMeta) {

        GuardianContext guardianContext = this.contextProvider.provide(invocationMeta);
        Method method = invocationMeta.getExecutor().getMethod();

        return this.guardian.allows(method.getDeclaringClass(), guardianContext)
            && this.guardian.allows(method, guardianContext);
    }

    @Override
    public void checkCall(@NonNull InvocationMeta invocationMeta) {

        GuardianContext guardianContext = this.contextProvider.provide(invocationMeta);
        Method method = invocationMeta.getExecutor().getMethod();

        List<GuardianViolation> violations = new ArrayList<>();
        violations.addAll(this.guardian.inspect(method.getDeclaringClass(), guardianContext));
        violations.addAll(this.guardian.inspect(method, guardianContext));

        if (violations.isEmpty()) {
            return;
        }

        throw new NoAccessException(violations.stream()
            .map(GuardianViolation::getMessage)
            .filter(message -> !message.isEmpty())
            .collect(Collectors.joining(", ")));
    }
}
