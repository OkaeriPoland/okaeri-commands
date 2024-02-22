package eu.okaeri.commands.bungee.handler;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommandsUnknownErrorEvent extends Event {
    private final CommandData data;
    private final Invocation invocation;
    private final Throwable cause;
    private final String errorId;
    private boolean sendMessage;
}
