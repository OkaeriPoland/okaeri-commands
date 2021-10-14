package eu.okaeri.commands.injector;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.injector.Injector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsInjector implements CommandsExtension {

    private final Injector injector;

    @Override
    public void register(Commands commands) {
        commands.missingArgumentHandler(new CommandsInjectorArgumentHandler(this.injector));
    }
}
