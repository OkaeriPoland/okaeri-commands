package eu.okaeri.commands.tasker;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsBukkitTasker implements CommandsExtension {

    private final BukkitTasker tasker;

    @Override
    public void register(Commands commands) {
        commands.missingArgumentHandler(new TaskerMissingArgumentHandler(this.tasker, commands.getMissingArgumentHandler()));
        commands.schedulingHandler(new TaskerBukkitSchedulingHandler(this.tasker));
    }
}
