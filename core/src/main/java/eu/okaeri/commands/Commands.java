package eu.okaeri.commands;

import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.registry.CommandsRegistry;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.CommandsTypes;
import eu.okaeri.commands.type.CommandsTypesPack;
import eu.okaeri.commands.type.resolver.TypeResolver;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public interface Commands {

    CommandsRegistry getRegistry();

    CommandsTypes getTypes();

    Commands register(Class<? extends CommandService> clazz);

    Commands register(CommandService service);

    Commands register(TypeResolver typeResolver);

    Commands register(CommandsTypesPack typesPack);

    <T> T call(String command) throws InvocationTargetException, IllegalAccessException;

    Optional<InvocationContext> invocationMatch(String command);

    InvocationMeta invocationPrepare(InvocationContext invocationContext, CommandContext commandContext);
}
