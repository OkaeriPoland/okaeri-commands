package eu.okaeri.commands.bukkit.handler;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class CommandsUnknownErrorEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandData data;
    private final Invocation invocation;
    private final Throwable cause;
    private final String errorId;
    private boolean sendMessage;

    public CommandsUnknownErrorEvent(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable cause, @NonNull String errorId, boolean sendMessage) {
        this(false, data, invocation, cause, errorId, sendMessage);
    }

    public CommandsUnknownErrorEvent(boolean isAsync, @NonNull CommandData data, @NonNull Invocation invocation, @NonNull Throwable cause, @NonNull String errorId, boolean sendMessage) {
        super(isAsync);
        this.data = data;
        this.invocation = invocation;
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
