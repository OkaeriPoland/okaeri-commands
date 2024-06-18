package eu.okaeri.commands.velocity.handler;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandsUnknownErrorEvent { // FIXME: ResultedEvent?
    private final CommandData data;
    private final Invocation invocation;
    private final Throwable cause;
    private final String errorId;
    private boolean sendMessage;
}
