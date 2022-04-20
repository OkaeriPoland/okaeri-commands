package eu.okaeri.commands.guard;

import eu.okaeri.acl.guardian.Guardian;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.guard.context.DefaultGuardianContextProvider;
import eu.okaeri.commands.guard.context.GuardianContextProvider;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class CommandsGuard implements CommandsExtension {

    private final @NonNull Guardian guardian;
    private @NonNull GuardianContextProvider contextProvider = new DefaultGuardianContextProvider();

    @Override
    public void register(Commands commands) {
        commands.accessHandler(new GuardAccessHandler(this.guardian, this.contextProvider));
    }
}
