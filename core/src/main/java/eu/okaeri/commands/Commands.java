package eu.okaeri.commands;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

public interface Commands {

    Commands register(Class<? extends CommandService> clazz);

    Commands register(CommandService service);

    List<CommandMeta> findByLabel(String label);

    List<CommandMeta> findByLabelAndArgs(String label, String args);

    @Deprecated
    Object call(String command) throws InvocationTargetException, IllegalAccessException;

    Optional<InvocationContext> invocationMatch(String command);

    InvocationMeta invocationPrepare(InvocationContext invocationContext, CommandContext commandContext);
}
