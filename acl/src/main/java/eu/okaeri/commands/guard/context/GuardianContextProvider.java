package eu.okaeri.commands.guard.context;

import eu.okaeri.acl.guardian.GuardianContext;
import eu.okaeri.commands.meta.InvocationMeta;
import lombok.NonNull;

@FunctionalInterface
public interface GuardianContextProvider {
    GuardianContext provide(@NonNull InvocationMeta invocationMeta);
}
