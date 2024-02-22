package eu.okaeri.commands.handler.argument;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;

import java.lang.reflect.Parameter;

public interface MissingArgumentHandler {
    Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index);
}
