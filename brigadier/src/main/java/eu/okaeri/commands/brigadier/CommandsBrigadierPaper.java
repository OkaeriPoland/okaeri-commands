package eu.okaeri.commands.brigadier;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.service.CommandData;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class CommandsBrigadierPaper extends CommandsBrigadierBase implements CommandsExtension, Listener {

    private final Plugin plugin;

    @Override
    public void register(Commands commands) {
        this.commands = (OkaeriCommands) commands;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void handleAsyncPlayerSendCommands(AsyncPlayerSendCommandsEvent event) {

        if (!event.isAsynchronous() && event.hasFiredAsync()) {
            return;
        }

        CommandData data = new CommandData();
        data.add("sender", event.getPlayer());

        this.update(data, event.getCommandNode());
    }
}
