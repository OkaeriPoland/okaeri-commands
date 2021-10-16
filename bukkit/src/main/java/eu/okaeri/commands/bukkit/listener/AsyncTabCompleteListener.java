package eu.okaeri.commands.bukkit.listener;

import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Locale;

public class AsyncTabCompleteListener implements EventExecutor {

    private final CommandsBukkit commands;
    private final MethodHandle AsyncTabCompleteEventGetSender;
    private final MethodHandle AsyncTabCompleteEventGetBuffer;
    private final MethodHandle AsyncTabCompleteEventIsCommand;
    private final MethodHandle AsyncTabCompleteEventSetCompletions;
    private final MethodHandle AsyncTabCompleteEventSetHandled;

    @SneakyThrows
    public AsyncTabCompleteListener(CommandsBukkit commands, Class<? extends Event> eventClass) {
        this.commands = commands;
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        this.AsyncTabCompleteEventGetSender = lookup.findVirtual(eventClass, "getSender", MethodType.methodType(CommandSender.class));
        this.AsyncTabCompleteEventGetBuffer = lookup.findVirtual(eventClass, "getBuffer", MethodType.methodType(String.class));
        this.AsyncTabCompleteEventIsCommand = lookup.findVirtual(eventClass, "isCommand", MethodType.methodType(boolean.class));
        this.AsyncTabCompleteEventSetCompletions = lookup.findVirtual(eventClass, "setCompletions", MethodType.methodType(void.class, List.class));
        this.AsyncTabCompleteEventSetHandled = lookup.findVirtual(eventClass, "setHandled", MethodType.methodType(void.class, boolean.class));
    }

    @Override
    @SneakyThrows
    public void execute(Listener listener, Event event) {

        boolean command = (boolean) this.AsyncTabCompleteEventIsCommand.bindTo(event).invoke();
        if (!command) {
            return;
        }

        CommandSender sender = (CommandSender) this.AsyncTabCompleteEventGetSender.bindTo(event).invoke();
        String buffer = (String) this.AsyncTabCompleteEventGetBuffer.bindTo(event).invoke();

        String[] parts = buffer.split(" ", 2);
        if (parts.length < 2) {
            return;
        }

        String label = parts[0].substring(1).toLowerCase(Locale.ROOT);
        String args = parts[1];

        List<CommandMeta> metas = this.commands.findByLabel(label);
        if (metas.isEmpty()) {
            return;
        }

        CommandContext commandContext = new CommandContext();
        commandContext.add("sender", sender);

        InvocationContext dummyContext = InvocationContext.of(label, args);
        List<String> completions = this.commands.complete(metas, dummyContext, commandContext);

        this.AsyncTabCompleteEventSetCompletions.bindTo(event).invoke(completions);
        this.AsyncTabCompleteEventSetHandled.bindTo(event).invoke(true);
    }
}
