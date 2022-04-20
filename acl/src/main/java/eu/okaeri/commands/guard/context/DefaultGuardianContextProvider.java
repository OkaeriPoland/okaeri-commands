package eu.okaeri.commands.guard.context;

import eu.okaeri.acl.guardian.GuardianContext;
import eu.okaeri.commands.meta.InvocationMeta;
import lombok.NonNull;

import java.lang.reflect.Method;

public class DefaultGuardianContextProvider implements GuardianContextProvider {

    @Override
    public GuardianContext provide(@NonNull InvocationMeta invocationMeta) {

        GuardianContext guardianContext = GuardianContext.of();
        Method method = invocationMeta.getExecutor().getMethod();
        Object[] call = invocationMeta.getCall();

        for (int i = 0; i < call.length; i++) {
            String argument = method.getParameters()[i].getName();
            guardianContext.with(argument, call[i]);
        }

        return guardianContext;
    }
}
