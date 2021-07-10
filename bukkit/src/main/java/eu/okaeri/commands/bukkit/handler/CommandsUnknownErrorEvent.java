package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class CommandsUnknownErrorEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandContext commandContext;
    private final InvocationContext invocationContext;
    private final Throwable cause;
    private final String errorId;
    private boolean sendMessage;

    public CommandsUnknownErrorEvent(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable cause, @NonNull String errorId, boolean sendMessage) {
        this(false, commandContext, invocationContext, cause, errorId, sendMessage);
    }

    public CommandsUnknownErrorEvent(boolean isAsync, @NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull Throwable cause, @NonNull String errorId, boolean sendMessage) {
        super(isAsync);
        this.commandContext = commandContext;
        this.invocationContext = invocationContext;
        this.cause = cause;
        this.errorId = errorId;
        this.sendMessage = sendMessage;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
