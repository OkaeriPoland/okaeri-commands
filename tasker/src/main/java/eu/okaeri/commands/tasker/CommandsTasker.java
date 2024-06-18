package eu.okaeri.commands.tasker;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.tasker.core.Tasker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsTasker implements CommandsExtension {

    private final Tasker tasker;

    @Override
    public void register(Commands commands) {
        MissingArgumentHandler currentHandler = commands.getMissingArgumentHandler();
        commands.missingArgumentHandler(new TaskerMissingArgumentHandler(this.tasker, currentHandler));
    }
}
