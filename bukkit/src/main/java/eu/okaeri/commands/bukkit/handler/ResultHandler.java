package eu.okaeri.commands.bukkit.handler;

import org.bukkit.command.CommandSender;

public interface ResultHandler {
    boolean onResult(Object result, CommandSender sender);
}
