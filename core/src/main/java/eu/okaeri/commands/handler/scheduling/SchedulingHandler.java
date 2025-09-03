package eu.okaeri.commands.handler.scheduling;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

public interface SchedulingHandler {
    public void run(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Runnable runnable);
}
