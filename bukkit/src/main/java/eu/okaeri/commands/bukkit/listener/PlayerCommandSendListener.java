package eu.okaeri.commands.bukkit.listener;

import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerCommandSendListener implements EventExecutor {

    private final CommandsBukkit commands;
    private final MethodHandle PlayerCommandSendEventGetCommands;
    private final MethodHandle PlayerCommandSendEventGetPlayer;

    @SneakyThrows
    public PlayerCommandSendListener(CommandsBukkit commands, Class<? extends Event> eventClass) {
        this.commands = commands;
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        this.PlayerCommandSendEventGetCommands = lookup.findVirtual(eventClass, "getCommands", MethodType.methodType(Collection.class));
        this.PlayerCommandSendEventGetPlayer = lookup.findVirtual(eventClass, "getPlayer", MethodType.methodType(Player.class));
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void execute(Listener listener, Event event) {

        Player player = (Player) this.PlayerCommandSendEventGetPlayer.bindTo(event).invoke();
        Collection<String> commands = (Collection<String>) this.PlayerCommandSendEventGetCommands.bindTo(event).invoke();

        CommandContext commandContext = new CommandContext();
        commandContext.add("sender", player);

        Set<String> disallowedLabels = this.commands.getRegisteredServices().values().stream()
            .filter(service -> {
                InvocationContext invocationContext = InvocationContext.of(service, service.getLabel(), new String[0]);
                return !this.commands.getAccessHandler().allowAccess(service, invocationContext, commandContext);
            })
            .flatMap(service -> {
                List<String> labels = new ArrayList<>();
                labels.add(service.getLabel());
                labels.add(service.getLabel() + ":" + service.getLabel());
                for (String alias : service.getAliases()) {
                    labels.add(alias);
                    labels.add(alias + ":" + alias);
                }
                return labels.stream();
            })
            .collect(Collectors.toSet());

        commands.removeIf(disallowedLabels::contains);
    }
}
