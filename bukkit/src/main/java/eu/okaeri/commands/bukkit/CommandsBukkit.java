package eu.okaeri.commands.bukkit;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.bukkit.annotation.Sender;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class CommandsBukkit extends CommandsAdapter {

    protected final CommandMap commandMap;

    public CommandsBukkit() {
        try {
            Server server = Bukkit.getServer();
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            this.commandMap = (CommandMap) commandMapField.get(server);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            exception.printStackTrace();
            throw new RuntimeException("cannot get commandMap", exception);
        }
    }

    @Override
    public Object resolveMissingArgument(CommandContext context, CommandMeta command, Parameter param, int i) {

        Class<?> paramType = param.getType();

        // TODO: player only command
        if (Player.class.isAssignableFrom(paramType) && (param.getAnnotation(Sender.class) != null)) {
            return context.get("sender", Player.class);
        }

        // TODO: console only command
        if (ConsoleCommandSender.class.isAssignableFrom(paramType)) {
            return context.get("sender", ConsoleCommandSender.class);
        }

        // other sender
        if (CommandSender.class.isAssignableFrom(paramType) && context.has("sender", CommandSender.class)) {
            return context.get("sender");
        }

        return super.resolveMissingArgument(context, command, param, i);
    }

    @Override
    public void onRegister(CommandMeta command) {

        ServiceMeta service = command.getService();
        String serviceLabel = service.getLabel();

        this.commandMap.register(serviceLabel, new Command(serviceLabel) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {

                String fullCommand = (label + " " + String.join(" ", args)).trim();
                CommandContext context = new CommandContext();
                context.add("sender", sender);

                try {
                    CommandsBukkit.super.getCore().call(fullCommand, context);
                } catch (InvocationTargetException | IllegalAccessException exception) {
                    exception.printStackTrace();
                }

                return true;
            }
        });

        super.onRegister(command);
    }
}
